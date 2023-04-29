package dk.itu.moapd.scootersharing.base.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.activities.*
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var latitudeTextField: TextInputLayout
    private lateinit var longitudeTextField: TextInputLayout
    private lateinit var timeTextField: TextInputLayout

    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database =
            Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
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

        startLocationAware()
    }

    private fun startLocationAware() {

        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(requireContext())

        locationCallback = object: LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                binding.contentMap.apply {
                    locationResult.lastLocation?.let { location ->
                        latitudeTextField.editText?.setText(location.latitude.toString())
                        longitudeTextField.editText?.setText(location.longitude.toString())
                        timeTextField.editText?.setText(location.time.toDateString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}