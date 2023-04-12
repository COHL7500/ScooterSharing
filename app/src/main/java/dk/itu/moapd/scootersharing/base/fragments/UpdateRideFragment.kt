package dk.itu.moapd.scootersharing.base.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.activities.MainActivity
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentUpdateRideBinding
import dk.itu.moapd.scootersharing.base.models.Scooter

class UpdateRideFragment : Fragment() {

    private var _binding: FragmentUpdateRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var scooterLocation: EditText
    private lateinit var updateRideButton: Button

    private lateinit var database: DatabaseReference

    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUpdateRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scooterLocation = binding.editTextLocation
        updateRideButton = binding.updateRideButton2
        scooterLocation.hint = adapter.getItem(0).where

        /**
         * Sets name and location of scooter, then clears the text fields.
         */
        updateRideButton.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert_title_UpdateRide))
                .setMessage(getString(R.string.alert_supporting_text_UpdateRide))

                .setNegativeButton(getString(R.string.decline)) { _, _ ->
                }
                .setPositiveButton(getString(R.string.accept)) { _, _ ->

                    val location = scooterLocation.text.toString().trim()

                    if (location.isNotEmpty()) {

                        adapter.getItem(0).where = location
                        scooterLocation.text.clear()

                        adapter.getRef(0).setValue(adapter.getItem(0))

                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}