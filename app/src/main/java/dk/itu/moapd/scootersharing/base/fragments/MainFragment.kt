/*
 * MIT License
 * Copyright (c) 2023 Bastjan Sejberg
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dk.itu.moapd.scootersharing.base.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.base.*
import dk.itu.moapd.scootersharing.base.activities.LoginActivity
import dk.itu.moapd.scootersharing.base.activities.StartRideActivity
import dk.itu.moapd.scootersharing.base.activities.UpdateRideActivity
import dk.itu.moapd.scootersharing.base.adapters.CustomFirebaseAdapter
import dk.itu.moapd.scootersharing.base.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.base.models.Scooter

/**
Class for binding the view and instantiating Scooter.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var startRideButton: Button
    private lateinit var updateRideButton: Button
    private lateinit var listRidesButton: Button
    private lateinit var signOutButton: Button
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: RecyclerView

    private lateinit var database: DatabaseReference


    companion object {
        lateinit var adapter: CustomFirebaseAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference

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

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        startRideButton = binding.startRideButton
        updateRideButton = binding.updateRideButton
        listRidesButton = binding.listRidesButton
        signOutButton = binding.signOutButton

        /**
         * Sets name and location of scooter, then clears the text fields.
         */
        startRideButton.setOnClickListener {
            val intent = Intent(activity, StartRideActivity::class.java)
            startActivity(intent)
        }

        updateRideButton.setOnClickListener {
            val intent = Intent(activity, UpdateRideActivity::class.java)
            startActivity(intent)
        }

        listRidesButton.setOnClickListener {
            binding.recyclerView.layoutManager = LinearLayoutManager(activity)
            binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
            binding.recyclerView.adapter = adapter
        }

        signOutButton.setOnClickListener {
            auth.signOut()

            Toast.makeText(activity, "User logged in the app.",
                Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)

        }

        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                  direction: Int) {
                super.onSwiped(viewHolder, direction)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_title_deleteRides))
                    .setMessage(getString(R.string.alert_supporting_text_deleteRides))

                    .setNegativeButton(getString(R.string.decline)) { _, _ ->
                    }
                    .setPositiveButton(getString(R.string.accept)) { _, _ ->
                        val adapter = recyclerView.adapter as CustomFirebaseAdapter

                        adapter.getRef(viewHolder.adapterPosition).removeValue()

                        Toast.makeText(viewHolder.itemView.context, "Ride deleted!",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                    .show()
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}