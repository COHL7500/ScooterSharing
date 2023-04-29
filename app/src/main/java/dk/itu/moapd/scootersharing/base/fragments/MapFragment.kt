package dk.itu.moapd.scootersharing.base.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.activities.*
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.utils.GeoHelper
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private lateinit var GeoHelper: GeoHelper
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var latitudeTextField: TextInputLayout
    private lateinit var longitudeTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout
    private lateinit var addressTextField: TextInputLayout

    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
        auth = FirebaseAuth.getInstance()
        GeoHelper = GeoHelper(requireContext())

        auth.currentUser?.let {
            val query = database.child("scooters")
                .orderByChild("name")

            val options = FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                .setLifecycleOwner(this)
                .build()

            adapter = CustomFirebaseAdapter(options)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitudeTextField = binding.contentMap.latitudeTextField
        longitudeTextField = binding.contentMap.longitudeTextField
        timeTextField = binding.contentMap.timeTextField
        addressTextField = binding.contentMap.addressTextField

        startLocationAware()
    }

    private fun startLocationAware() {
        GeoHelper.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                binding.contentMap.apply {
                    locationResult.lastLocation?.let { location ->
                        latitudeTextField.editText?.setText(location.latitude.toString())
                        longitudeTextField.editText?.setText(location.longitude.toString())
                        timeTextField.editText?.setText(location.time.toDateString())
                        addressTextField.editText?.setText(GeoHelper.getAddress(location.latitude,location.longitude))
                    }
                }
            }

            private fun Long.toDateString(): String {
                val date = Date(this)
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                return format.format(date)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GeoHelper.subscribeToLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        GeoHelper.unsubscribeToLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}