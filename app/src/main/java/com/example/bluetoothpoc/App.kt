package com.example.bluetoothpoc

import android.app.Application

class App : Application() {
    private var state: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun setBluetoothState(flag: Boolean) {
        state = flag
    }

    fun getBluetoothState() = state

    companion object {
        private var instance: App? = null

        fun getApplication(): App {
            return instance ?: App()
        }
    }
}
