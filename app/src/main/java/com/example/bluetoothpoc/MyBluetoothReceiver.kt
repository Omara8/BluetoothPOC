package com.example.bluetoothpoc

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import java.util.Date

class MyBluetoothReceiver : BroadcastReceiver() {

    private lateinit var locationUtils: LocationUtils

    @SuppressLint("MissingPermission")
    override fun onReceive(p0: Context?, p1: Intent?) {
        locationUtils = LocationUtils()
        locationUtils.init(p0!!)
        val action = p1?.action
        val device: BluetoothDevice? = p1?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        var connectionName = ""
        var message = if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            App.getApplication().setBluetoothState(true)
            connectionName = "Connected On"
            "${device?.name} is Connected"
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            App.getApplication().setBluetoothState(false)
            connectionName = "Disconnected On"
            "${device?.name} is Disconnected"
        } else {
            App.getApplication().setBluetoothState(false)
            "Connected On"
            ""
        }

        val intent = Intent(p0, StickyService::class.java)
        intent.putExtra("BT_DEVICE", "${device?.address} $message")
        Analytics.logIntentFromReceiverToService(intent)
        Analytics.logContextInsideReceiver(p0)
        p0.startForegroundService(intent)

//        val activity = Intent(p0, MainActivity::class.java)
//        activity.flags = FLAG_ACTIVITY_NEW_TASK
//        p0.startActivity(activity)


        locationUtils.getCurrentLocation(p0, locationSuccess = {
            message += "Latitude: ${it.latitude} / Longitude: ${it.longitude}"
            saveFile(p0, "$connectionName ${Date()}", message, ".txt")
        }, locationFailure = {
            saveFile(p0, "No Location $connectionName ${Date()}", message, ".txt")
        })
    }
}

object Helper {
    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }
}