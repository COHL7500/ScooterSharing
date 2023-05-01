package dk.itu.moapd.scootersharing.base.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationResult

abstract class GeoClass : Fragment() {
    protected lateinit var geoHelper: GeoHelper
    protected var geoBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GeoHelper.GeoBinder
            geoHelper = binder.getService()
            geoBound = true
            geoHelper.setCallback(::startLocationAware)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            geoBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), GeoHelper::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        requireContext().unbindService(connection)
        geoBound = false
    }

    abstract fun startLocationAware(locationResult: LocationResult)
}