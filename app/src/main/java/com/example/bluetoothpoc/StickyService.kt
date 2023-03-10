package com.example.bluetoothpoc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StickyService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state: Boolean = App.getApplication().getBluetoothState()
        val content = if (state) "Bluetooth Connected" else "Bluetooth Disconnected"
        Analytics.logBluetoothConnection(state)
        Analytics.logStickyServiceContext(this)

        val message = intent?.extras?.getString("BT_DEVICE")

        val notificationChannel = NotificationChannel("88", "BluetoothPOC", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        var builder = NotificationCompat.Builder(this, "88")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Bluetooth Connection State")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (state) {
            Analytics.logNotificationState(true)
            startForeground(888, builder.build())
        } else {
            Analytics.logNotificationState(false)
            notificationManager.cancel(888)
            this.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}