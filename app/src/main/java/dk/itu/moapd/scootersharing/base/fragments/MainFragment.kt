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

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.launch
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dk.itu.moapd.scootersharing.base.R
import dk.itu.moapd.scootersharing.base.activities.*
import dk.itu.moapd.scootersharing.base.contracts.CameraContract
import dk.itu.moapd.scootersharing.base.databinding.FragmentMainBinding
import dk.itu.moapd.scootersharing.base.models.Scooter
import dk.itu.moapd.scootersharing.base.services.LocationService
import dk.itu.moapd.scootersharing.base.utils.GeoClass
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
Class for binding the view and instantiating Scooter.
 */
class MainFragment : GeoClass() {

    private var _binding: FragmentMainBinding? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val binding
        get() = checkNotNull(_binding) {

        }

    private lateinit var startRideButton: Button
    private lateinit var listRidesButton: Button
    private lateinit var signOutButton: Button
    private lateinit var cameraButton: Button
    private lateinit var qrscanButton: Button
    private lateinit var mapButton: Button
    private lateinit var bucket: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    companion object {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = Firebase.database("https://moapd-2023-6e1fd-default-rtdb.europe-west1.firebasedatabase.app/").reference
        bucket = FirebaseStorage.getInstance().reference
    }

    private val uploadLastScooterPhoto = registerForActivityResult(CameraContract()) { bitmap ->
        bitmap?.let {
            Log.d("BITMAP_SUCCESS", bitmap.toString())

            val lastPhotoRef = bucket.child("last_photo_scooters")
            val baos = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()
            val uploadTask = lastPhotoRef.child("last-photo.jpg").putBytes(data)

            uploadTask.addOnSuccessListener {
                Log.d("FirebaseBucket", "Image uploaded successfully")
            }
        }
    }

    private val scanQRCodePhoto = registerForActivityResult(CameraContract()) { bitmap ->
        bitmap?.let {
            val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build())
            scanner.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { barcodes ->
                    val qrCodeVal = barcodes.first().rawValue.toString()
                    Log.d("QR_SCANNED_SUCCESS",qrCodeVal)
                    startRideButtonClicked(qrCodeVal)
                }.addOnFailureListener {
                    Log.d("QR_SCANNED_FAILURE","")
                }
        }
    }

    private fun startRideButtonClicked(scooterId: String) {
        database.child("scooters").child(scooterId).get().addOnSuccessListener {
            if(it.child("isRented").value == false){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_title_startRide))
                    .setMessage(getString(R.string.alert_supporting_text_startRide))
                    .setNegativeButton(getString(R.string.decline)) { _, _ -> }
                    .setPositiveButton(getString(R.string.accept)) { _, _ ->
                        auth.currentUser?.let { _ ->
                            database.child("scooters").child(scooterId).let {
                                it.child("isRented").setValue(true)
                                it.child("timestamp").setValue(System.currentTimeMillis())
                                it.child("startLatitude").setValue(latitude)
                                it.child("startLongitude").setValue(longitude)
                            }
                        }
                    }.show()
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.alert_title_scooterAlreadyRented)
                    .setMessage(R.string.alert_title_scooterAlreadyRented)
                    .setPositiveButton(getString(R.string.accept)) { _, _ -> }
                    .show()
            }
        }.addOnFailureListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.alert_title_scooterNotFound)
                .setMessage(R.string.alert_title_scooterNotFound)
                .setPositiveButton(getString(R.string.accept)) { _, _ -> }
                .show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startRideButton = binding.startRideButton
        listRidesButton = binding.listRidesButton
        signOutButton = binding.signOutButton
        mapButton = binding.mapButton
        cameraButton = binding.cameraButton
        qrscanButton = binding.qrscanButton

        startRideButton.setOnClickListener {
            val intent = Intent(activity, CreateRideActivity::class.java)
            startActivity(intent)
        }

        listRidesButton.setOnClickListener {
            val intent = Intent(activity, ListRidesActivity::class.java)
            startActivity(intent)
        }

        signOutButton.setOnClickListener {
            auth.signOut()

            Toast.makeText(activity, "User logged out of the app.",
                Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)

        }

        mapButton.setOnClickListener {
            val intent = Intent(activity, MapActivity::class.java)
            startActivity(intent)
        }

        cameraButton.setOnClickListener {
            uploadLastScooterPhoto.launch(Unit)
        }

        qrscanButton.setOnClickListener {
            scanQRCodePhoto.launch(Unit)
        }

        requestUserPermissions()
    }

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {

        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission)

        return result
    }

    private fun requestUserPermissions() {

        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.CAMERA)

        val permissionsToRequest = permissionsToRequest(permissions)

        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}