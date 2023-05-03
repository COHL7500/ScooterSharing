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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.services.LocationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference
    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val time = intent.getLongExtra("time", 0)

            setAddress(latitude, longitude, time)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val fragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        fragment?.getMapAsync(this)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationService = LocationService()
    }

    private fun setAddress(latitude: Double, longitude: Double, time: Long) {
        binding.contentMap.apply {

        }
    }

    private fun Long.toDateString(): String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    override fun onResume() {
        super.onResume()

        requireActivity().startService(Intent(requireContext(), LocationService::class.java))

        val filter = IntentFilter("location_result")
        broadcastManager.registerReceiver(locationReceiver, filter)
    }

    override fun onPause() {
        super.onPause()

        requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.unregisterReceiver(locationReceiver)
    }

    private fun bitMapFromVector(vectorResID:Int): BitmapDescriptor {
        val vectorDrawable= ContextCompat.getDrawable(requireContext(),vectorResID)
        vectorDrawable!!.setBounds(0,0, vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMapReady(googleMap: GoogleMap){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
            return

        googleMap.isMyLocationEnabled = true
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        // Runs a coroutine on the IO thread, thus preventing blocking the UI (main) thread.
        // This is necessary as onMapReady runs asynchronously.

        lifecycleScope.launch(Dispatchers.IO) {
            var scooterList: List<Scooter>

            database.child("scooters").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    scooterList = it.result.children.mapNotNull { doc ->
                        doc.getValue(Scooter::class.java)
                    }

                    // Use a coroutine to add the markers asynchronously
                    lifecycleScope.launch(Dispatchers.Main) {
                        scooterList.forEach { scooter ->
                            val scooterMarker = MarkerOptions()
                                .position(LatLng(scooter.startLatitude!!, scooter.startLongitude!!))
                                .title(scooter.name)
                                .icon(bitMapFromVector(R.drawable.scooter_marker_noarrow_icon_32))
                            googleMap.addMarker(scooterMarker)
                        }
                    }
                } else {
                    Log.d("MAP_SCOOTER_ERROR", it.exception?.message.toString())
                }
            }
        }

        val itu = LatLng(55.6596, 12.5910)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 13f))
    }


    private fun Address.toAddressString() : String {
        val address = this
        val stringBuilder = StringBuilder()
        stringBuilder.apply {
            append(address.getAddressLine(0)).append("\n")
            append(address.locality).append("\n")
            append(address.postalCode).append("\n")
            append(address.countryName)
        }
        return stringBuilder.toString()
    }

    @Suppress("DEPRECATION")
    fun getAddress(latitude: Double, longitude: Double) : String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.toAddressString()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}