package dk.itu.moapd.scootersharing.base.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dk.itu.moapd.scootersharing.base.services.LocationService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class GeoClass: Fragment() {
    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder
    protected lateinit var coordinates: Pair<Double,Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())
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
        val date = Date(this)
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(date)
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

    override fun onResume() {
        super.onResume()
        requireActivity().startService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.registerReceiver(locationReceiver, IntentFilter("location_result"))
    }

    override fun onPause() {
        super.onPause()
        requireActivity().stopService(Intent(requireContext(), LocationService::class.java))
        broadcastManager.unregisterReceiver(locationReceiver)
    }
}