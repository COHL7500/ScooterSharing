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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.utils.GeoClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : GeoClass(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference
    private lateinit var scooters: List<Scooter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance().reference
        database.child("scooters").get().addOnCompleteListener {
            if (it.isSuccessful) {
                scooters = it.result.children.mapNotNull { doc ->
                    doc.getValue(Scooter::class.java)
                }
            } else {
                Log.d("MAP_SCOOTER_ERROR", it.exception?.message.toString())
            }
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

    private fun bitMapFromVector(vectorResID:Int): BitmapDescriptor {
        val vectorDrawable= ContextCompat.getDrawable(requireContext(),vectorResID)
        vectorDrawable!!.setBounds(0,0, vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight)
        val bitmap=
            Bitmap.createBitmap(vectorDrawable.intrinsicWidth,vectorDrawable.intrinsicHeight,Bitmap.Config.ARGB_8888)
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
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}