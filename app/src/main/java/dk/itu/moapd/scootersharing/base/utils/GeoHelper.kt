/*
 * MIT License
 *
 * Copyright (c) 2023 Fabricio Batista Narcizo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dk.itu.moapd.scootersharing.base.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class GeoHelper(private val context: Context) {
    var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    lateinit var locationCallback: LocationCallback

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

    fun setAddress(addressTextField: TextInputLayout, latitude: Double, longitude: Double) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val geocodeListener = Geocoder.GeocodeListener { addresses ->
            addresses.firstOrNull()?.toAddressString()?.let { address ->
                addressTextField?.editText?.setText(address)
            }
        }
        if (Build.VERSION.SDK_INT >= 33)
            geocoder.getFromLocation(latitude, longitude, 1, geocodeListener)
        else
            geocoder.getFromLocation(latitude, longitude, 1)?.let { addresses ->
                addresses.firstOrNull()?.toAddressString()?.let { address ->
                    addressTextField?.editText?.setText(address)
                }
            }
    }

    private fun checkPermission() =
        PermissionChecker.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PermissionChecker.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun subscribeToLocationUpdates() {
        if(checkPermission()) return
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5).build()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun unsubscribeToLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}