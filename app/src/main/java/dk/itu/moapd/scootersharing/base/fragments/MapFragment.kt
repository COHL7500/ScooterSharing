package dk.itu.moapd.scootersharing.base.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.services.LocationService
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var latitudeTextField: TextInputLayout
    private lateinit var longitudeTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout
    private lateinit var addressTextField: TextInputLayout
    private lateinit var locationService: LocationService
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var geoCoder: Geocoder

    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
        broadcastManager = LocalBroadcastManager.getInstance(requireContext())
        geoCoder = Geocoder(requireContext(), Locale.getDefault())
        auth = FirebaseAuth.getInstance()

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitudeTextField = binding.contentMap.latitudeTextField
        longitudeTextField = binding.contentMap.longitudeTextField
        timeTextField = binding.contentMap.timeTextField
        addressTextField = binding.contentMap.addressTextField

        locationService = LocationService()
    }

    private fun setAddress(latitude: Double, longitude: Double, time: Long) {

            binding.contentMap.apply {
                longitudeTextField.editText?.setText(longitude.toString())
                latitudeTextField.editText?.setText(latitude.toString())
                timeTextField.editText?.setText(time.toDateString())
                addressTextField.editText?.setText(getAddress(latitude, longitude))
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