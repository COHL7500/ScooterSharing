package dk.itu.moapd.scootersharing.base.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.activities.MainActivity
import dk.itu.moapd.scootersharing.base.databinding.FragmentRentedRideBinding
import dk.itu.moapd.scootersharing.base.services.LocationService
import java.text.SimpleDateFormat
import java.util.*

class RentedRideFragment : GeoClass(), OnMapReadyCallback {

    private var _binding: FragmentRentedRideBinding? = null
    private var scooterId: String = ""
    private var userId: String = ""
    private var scooter: Scooter? = null

    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference

    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder
    private var maxAcceleration: Float = 0.0f
    private var latestLongitude: Double = 55.0
    private var latestLatitude: Double = 56.0
    private var latestTime: Long = 0
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerationSensor: Sensor
    private lateinit var googleMap: GoogleMap
    private lateinit var endRideButton: Button
    private lateinit var userMarker: Marker
    private lateinit var bucket: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRentedRideBinding.inflate(inflater, container, false)

        val fragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        fragment?.getMapAsync(this)

        auth.currentUser?.let { user ->
           database.child("scooters").orderByChild("rentedBy").equalTo(user.uid).get().addOnSuccessListener{
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
        bitmap?.let {
            Log.d("BITMAP_SUCCESS", bitmap.toString())

            val lastPhotoRef = bucket.child("last_photo_scooters")
            val baos = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()
            val uploadTask = lastPhotoRef.child("${scooterId}_${userId}_${System.currentTimeMillis()}.jpg").putBytes(data)

            uploadTask.addOnSuccessListener {
                stopScooterRide(scooterId, userId)
                Log.d("FirebaseBucket", "Image uploaded successfully")
            }.addOnFailureListener {
                Log.e("FIREBASE_BUCKET", "Could not upload image")
            }
        }
    }

    private fun stopScooterRide(scooterId: String, userID: String){
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
                    scooter.child("timestamp").setValue(System.currentTimeMillis())
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }
                }.show()
        }
    }

    private val accelerationListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            val ax = sensorEvent.values[0]
            val ay = sensorEvent.values[1]
            val az = sensorEvent.values[2]
            val total = sqrt(
                ax.pow(2) +
                        ay.pow(2) +
                        az.pow(2)
            )
            if (total > maxAcceleration)
                maxAcceleration = total

            Log.d("WOOOHOOOOOOOO ", maxAcceleration.toString())
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        }
    }



    private fun Long.toDateString(): String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    private fun bitMapFromVector(vectorResID:Int): BitmapDescriptor {
        val vectorDrawable= ContextCompat.getDrawable(requireContext(),vectorResID)
        vectorDrawable!!.setBounds(0,0, vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            coordinates = Pair(latitude,longitude)

            database.child("scooters").child(scooterId).get().addOnSuccessListener {
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(latitude, longitude),
                        15f
                    )
                )
                userMarker.position = LatLng(latitude, longitude)
        val filter = IntentFilter("location_result")
        broadcastManager.registerReceiver(locationReceiver, filter)

        sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

                val distance = FloatArray(3)

                distanceBetween(latitude, longitude, coordinates.first, coordinates.second, distance)
                binding.distanceRentedText.text = "${(String.format("%.2f",distance[0]/1000)).toString()} km"
                binding.nameRentedTextview.text = scooter?.name
                binding.timeRentedTextview.text = (scooter?.timestamp?.let { it1 ->
                    System.currentTimeMillis().minus(
                        it1
                    )
                })?.toDateString()
            }
        }
    override fun onPause() {
        super.onPause()
        requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.unregisterReceiver(locationReceiver)

        sensorManager.unregisterListener(accelerationListener)
    }

    override fun onMapReady(googleMap: GoogleMap){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
            return

        val defLocITU = LatLng(55.6596, 12.5910)

        this.googleMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        userMarker = googleMap.addMarker(MarkerOptions()
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