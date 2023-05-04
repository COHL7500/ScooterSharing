package dk.itu.moapd.scootersharing.base.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dk.itu.moapd.scootersharing.base.services.LocationService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow
import kotlin.math.sqrt

abstract class GeoClass: Fragment() {
    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerationSensor: Sensor
    private var lastTimestamp: Long = 0
    private var lastSpeed: Double = 0.0
    protected var speed: Double = 0.0
    private var maxSpeed: Double = 0.0 // String.format("%.2f", maxSpeed).toDouble()
    protected lateinit var coordinates: Pair<Double,Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationService = LocationService()
    }

    protected open val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            coordinates = Pair(latitude,longitude)
        }
    }

    protected fun Long.toDateString(): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        format.setTimeZone(TimeZone.getTimeZone("UTC"))
        return format.format(this)
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

    // km/h
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

            val timestamp = System.currentTimeMillis()
            val timeInterval = (timestamp - lastTimestamp) / 1000.0
            lastTimestamp = timestamp

            val acceleration = total * 9.81
            val velocity = lastSpeed + acceleration * timeInterval
            speed = velocity * 3.6
            lastSpeed = velocity

            if (speed > maxSpeed)
                maxSpeed = speed
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        }
    }

    @Suppress("DEPRECATION")
    fun getAddress(latitude: Double, longitude: Double) : String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.toAddressString()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().startService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.registerReceiver(locationReceiver, IntentFilter("location_result"))

        sensorManager.registerListener(accelerationListener, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.unregisterReceiver(locationReceiver)
        sensorManager.unregisterListener(accelerationListener)
    }
}