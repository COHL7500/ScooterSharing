package dk.itu.moapd.scootersharing.base.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
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
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class RentedRideFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRentedRideBinding? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            latestLatitude = intent.getDoubleExtra("latitude", 0.0)
            latestLongitude = intent.getDoubleExtra("longitude", 0.0)
            latestTime = intent.getLongExtra("time", 0)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latestLatitude, latestLongitude), 15f))

            userMarker.position = LatLng(latestLatitude, latestLongitude)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRentedRideBinding.inflate(inflater, container, false)

        val fragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        fragment?.getMapAsync(this)

        endRideButton = binding.endRideButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationService = LocationService()

        endRideButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("End Ride")
                .setMessage("Are you sure you want to end the ride?")
                .setNegativeButton(getString(R.string.decline)) { _, _ -> }
                .setPositiveButton(getString(R.string.accept)) { _, _ ->

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
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

    override fun onResume() {
        super.onResume()

        requireActivity().startService(Intent(requireContext(), LocationService::class.java))

        val filter = IntentFilter("location_result")
        broadcastManager.registerReceiver(locationReceiver, filter)

        sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)
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

        this.googleMap = googleMap

        val result : FloatArray = FloatArray(3)

        Log.d("VALUE_DISTANCE_BETWEEN",  result.contentToString())

        android.location.Location.distanceBetween(55.6596,12.5910, 60.9, 15.3, result)

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val itu = LatLng(55.6596, 12.5910)

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        userMarker = googleMap.addMarker(MarkerOptions()
            .position(itu)
            .icon(bitMapFromVector(R.drawable.scooter_marker_icon_32))
        )!!

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itu, 15f))
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