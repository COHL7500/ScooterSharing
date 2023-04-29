package dk.itu.moapd.scootersharing.base.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.activities.MainActivity
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import java.util.*

class StartRideFragment : Fragment() {

    private var _binding: FragmentStartRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var scooterName: EditText
    private lateinit var scooterLocation: EditText
    private lateinit var startRideButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
        auth = FirebaseAuth.getInstance()

        auth.currentUser?.let {
            val query = database.child("scooters")
                .child(it.uid)
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

        _binding = FragmentStartRideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scooterName = binding.editTextName
        scooterLocation = binding.editTextLocation
        startRideButton = binding.startRideButton

        /**
         * Sets name and location of scooter, then clear the text fields.
         */
        startRideButton.setOnClickListener {

            if (scooterName.text.isNotEmpty() &&
                scooterLocation.text.isNotEmpty()
            ) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_title_startRide))
                    .setMessage(getString(R.string.alert_supporting_text_startRide))

                    .setNegativeButton(getString(R.string.decline)) { _, _ ->
                    }
                    .setPositiveButton(getString(R.string.accept)) { _, _ ->

                        val name = scooterName.text.toString()

                        if (name.isNotEmpty()) {
                            val timestamp = randomDate()
                            val scooter = Scooter(name, scooterLocation.text.toString(), timestamp, "scooter_thumbnail.png")

                            auth.currentUser?.let { user ->
                                val uid = database.child("scooters")
                                    .push()
                                    .key

                                uid?.let {
                                    database.child("scooters")
                                        .child(it)
                                        .setValue(scooter)
                                }
                            }
                        }

                        showMessage()

                        scooterName.text.clear()
                        scooterLocation.text.clear()

                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)

                    }.show()
            } else {

                Toast.makeText(context, "Please fill out both fields.",
                Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * A message confirming the scooter location and name.
     */
    private fun showMessage() {

        val message = "Ride started using ${scooterName.text}."

        Toast.makeText(context, message,
            Toast.LENGTH_SHORT)
            .show()

    }

    private fun randomDate(): Long {
        val random = Random()
        val now = System.currentTimeMillis()
        val year = random.nextDouble() * 1000 * 60 * 60 * 24 * 365
        return (now - year).toLong()
    }
}