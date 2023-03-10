package com.example.bluetoothpoc

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Checks for a set of permissions. If not granted, the user is asked to grant them.
 *
 * @param activity The activity that is requesting the permissions
 * @param permissions The permissions to be checked
 * @param requestCode The request code to be used when requesting the permissions
 */
fun checkPermissions(activity: Activity, permissions: Array<out String>, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, permissions, requestCode)
}

/**
 * Checks whether a set of permissions is granted or not
 *
 * @param context The context to be used for checking the permissions
 * @param permissions The permissions to be checked
 *
 * @return true if all permissions are granted, false otherwise
 */
fun checkPermissionsGranted(context: Context, permissions: Array<out String>): Boolean {
    permissions.forEach {
        when {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED ->
                return@checkPermissionsGranted false
        }
    }
    return true
}