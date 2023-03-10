package com.example.bluetoothpoc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Date

object Analytics {

    private var firebaseAnalytics: FirebaseAnalytics

    init {
        firebaseAnalytics = App.getApplication().applicationContext?.let {
            FirebaseAnalytics.getInstance(it)
        }!!
    }

    private fun buildBundle(date: String, eventType: String): Bundle {
        val bundle = Bundle()
        bundle.putString("DATE", date)
        bundle.putString("TYPE", eventType)
        return bundle
    }

    private fun logEvent(event: String, eventType: String) {
        firebaseAnalytics.logEvent(event, buildBundle(Date().toString(), eventType))
    }

    fun logBluetoothConnection(state: Boolean) {
        logEvent("Bluetooth_Connection_State", state.toString())
    }

    fun logNotificationState(shouldShow: Boolean) {
        logEvent("Show_Notification", shouldShow.toString())
    }

    fun logStickyServiceContext(service: Context) {
        logEvent("Service_Context", service.toString())
    }

    fun logIntentFromReceiverToService(intent: Intent) {
        logEvent("Intent_From_Receiver_To_Service", intent.toString())
    }

    fun logContextInsideReceiver(context: Context) {
        logEvent("Context_Inside_Receiver", context.toString())
    }

}