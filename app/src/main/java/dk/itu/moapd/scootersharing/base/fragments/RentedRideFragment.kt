package dk.itu.moapd.scootersharing.base.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.activities.MainActivity
import dk.itu.moapd.scootersharing.base.contracts.CameraContract
import dk.itu.moapd.scootersharing.base.databinding.FragmentRentedRideBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.services.LocationService
import dk.itu.moapd.scootersharing.base.utils.GeoClass
import java.io.ByteArrayOutputStream
import java.util.*

class RentedRideFragment : GeoClass(), OnMapReadyCallback {

    private var _binding: FragmentRentedRideBinding? = null
    private var scooterId: String = ""
    private var userId: String = ""
    private var scooter: Scooter? = null

    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder
    private lateinit var googleMap: GoogleMap
    private lateinit var userMarker: Marker
    private lateinit var bucket: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        bucket = FirebaseStorage.getInstance().reference
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRentedRideBinding.inflate(inflater, container, false)

        val fragment =
            childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        fragment?.getMapAsync(this)

        auth.currentUser?.let { user ->
            database.child("scooters").orderByChild("rentedBy").equalTo(user.uid).get()
                .addOnSuccessListener {
                    scooterId = it.children.first().key.toString()
                    scooter = it.child(scooterId).getValue(Scooter::class.java)
                    userId = user.uid
                }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationService = LocationService()

        binding.endRideButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("End Ride")
                .setMessage("Are you sure you want to end the ride?")
                .setNegativeButton(getString(R.string.decline)) { _, _ -> }
                .setPositiveButton(getString(R.string.accept)) { _, _ ->
                    uploadLastScooterPhoto.launch(Unit)
                }.show()
        }
    }

    private val uploadLastScooterPhoto = registerForActivityResult(CameraContract()) { bitmap ->
        Toast.makeText(activity, "Take a picture of Scooter!", Toast.LENGTH_SHORT).show()
        bitmap?.let {
            Log.d("BITMAP_SUCCESS", bitmap.toString())

            val scooterTimestamp = System.currentTimeMillis()
            val lastPhotoRef = bucket.child("last_photo_scooters")
            val baos = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()
            val uploadTask = lastPhotoRef.child("${scooterId}.jpg").putBytes(data)

            uploadTask.addOnSuccessListener {
                stopScooterRide(scooterId, scooterTimestamp)
                Log.d("FirebaseBucket", "Image uploaded successfully")
            }.addOnFailureListener {
                Log.e("FIREBASE_BUCKET", "Could not upload image")
            }
        }
    }

    private fun stopScooterRide(scooterId: String, timestamp: Long) {
        val scooter = database.child("scooters").child(scooterId)
        scooter.get().addOnSuccessListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Scooter no longer rented!")
                .setMessage("The scooter '${it.child("name").value}' is no longer being rented!")
                .setPositiveButton("Accept") { _, _ ->
                    scooter.child("rentedBy").setValue("")
                    scooter.child("isRented").setValue(false)
                    scooter.child("startLatitude").setValue(coordinates.first)
                    scooter.child("startLongitude").setValue(coordinates.second)
                    scooter.child("timestamp").setValue(timestamp)
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }.show()
        }
    }

    private fun bitMapFromVector(vectorResID: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResID)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap =
            Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override val locationReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {

            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val distance = distanceTo(
                latitude,
                longitude,
                scooter?.startLatitude!!,
                scooter?.startLongitude!!
            )[0] / 1000

            coordinates = Pair(latitude, longitude)
            database.child("scooters").child(scooterId).get().addOnSuccessListener {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(latitude, longitude),
                        15f
                    )
                )
                userMarker.position = LatLng(latitude, longitude)
                binding.distanceRentedText.text = "${"%.2f".format(distance)} km"
                binding.nameRentedTextview.text = scooter?.name
                binding.speedRentedTextview.text = "${"%.2f".format(speed / 100000000)} km/h"
                binding.timeRentedTextview.text = (scooter?.timestamp?.let { it1 ->
                    System.currentTimeMillis().minus(
                        it1
                    )
                })?.toDateString()
                binding.priceRentedTextview.text = "${"%.2f".format(distance * 50)} kr,-"
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
            return

        val defLocITU = LatLng(55.6596, 12.5910)

        this.googleMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        userMarker = googleMap.addMarker(
            MarkerOptions()
                .position(defLocITU)
                .icon(bitMapFromVector(R.drawable.scooter_marker_icon_32))
        )!!

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defLocITU, 15f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}