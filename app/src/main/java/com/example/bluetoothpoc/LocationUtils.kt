package com.example.bluetoothpoc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationUtils {

    private var fusedLocationProvider: FusedLocationProviderClient? = null

    fun init(context: Context) {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getCurrentLocation(context: Context, locationSuccess: (location: Location) -> Unit, locationFailure: () -> Unit) {
        val currentLocationRequest =
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationFailure()
                return
            } else {
                fusedLocationProvider?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            }
        currentLocationRequest?.let { task ->
            task.addOnSuccessListener {
                if (it != null)
                    locationSuccess(it)
                else
                    locationFailure()
            }
                .addOnFailureListener {
                    locationFailure()
                }
        }
    }
}