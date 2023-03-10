package com.example.bluetoothpoc

import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.S)
class MyCompanionService: CompanionDeviceService() {

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d("MyCompanionService", "Rebind ${intent.toString()}")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("MyCompanionService", "Unbind ${intent.toString()}")
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyCompanionService", "onStartCommand ${intent.toString()}")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        Log.d("MyCompanionService", "onStart ${intent.toString()}")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyCompanionService", "onCreate")
    }

    override fun onDeviceAppeared(address: String) {
        super.onDeviceAppeared(address)
        Log.d("MyCompanionService", "onDeviceAppeared ${address}")
    }

    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        super.onDeviceAppeared(associationInfo)
        Log.d("MyCompanionService", "onDeviceAppeared ${associationInfo}")
    }

    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        super.onDeviceDisappeared(associationInfo)
        Log.d("MyCompanionService", "onDeviceDisappeared ${associationInfo}")
    }

    override fun onDeviceDisappeared(address: String) {
        super.onDeviceDisappeared(address)
        Log.d("MyCompanionService", "onDeviceDisappeared ${address}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyCompanionService", "onDestroy")
    }
}