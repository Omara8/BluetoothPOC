package com.example.bluetoothpoc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.material.button.MaterialButton
import java.util.Date

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    private var devices: MutableList<String> = mutableListOf()
    private var listDevicesRecycler: RecyclerView? = null
    private var pairedDevicesRecycler: RecyclerView? = null
    private lateinit var btManager: BluetoothManager
    private lateinit var bleScanManager: BleScanManager
    private var scan: MaterialButton? = null
    private var companion: MaterialButton? = null
    private lateinit var locationUtils: LocationUtils


    val android13Permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    @RequiresApi(Build.VERSION_CODES.S)
    val android12Permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    val android11AndLowerPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    private val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android13Permissions else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) android12Permissions else android11AndLowerPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listDevicesRecycler = findViewById(R.id.list_devices)
        pairedDevicesRecycler = findViewById(R.id.paired_devices)
        scan = findViewById(R.id.scan)
        companion = findViewById(R.id.companion)
        locationUtils = LocationUtils()
        locationUtils.init(this)

        val adapter = DevicesAdapter(this, devices)

        checkForStoragePermission()

        companion?.setOnClickListener {
            val intent = Intent(this, CompanionActivity::class.java)
            startActivity(intent)
        }

        when (checkPermissionsGranted(
            this,
            permissions
        )) {
            true -> {
                scan?.isEnabled = true
            }
            false -> {
                scan?.isEnabled = false
                checkPermissions(
                    this, permissions, BLE_PERMISSION_REQUEST_CODE
                )
            }
        }

        listDevicesRecycler?.adapter = adapter
        listDevicesRecycler?.layoutManager = LinearLayoutManager(this)

        scan?.setOnClickListener {
            // Checks if the required permissions are granted and starts the scan if so, otherwise it requests them
            when (checkPermissionsGranted(
                this,
                permissions
            )) {
                true -> {
                    initBleManager(adapter)
                    bleScanManager.scanBleDevices()
                }
                false -> checkPermissions(
                    this, permissions, BLE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun initBleManager(adapter: DevicesAdapter) {
        // BleManager creation
        btManager = getSystemService(BluetoothManager::class.java)
        bleScanManager = BleScanManager(btManager, 30000, scanCallback = BleScanCallback({
            val name = "Address: ${it?.device?.address} / Name: ${it?.device?.name}"
            if (name.isNullOrBlank()) return@BleScanCallback

            if (!devices.contains(name)) {
                devices.add(name)
                adapter.notifyItemInserted(devices.size - 1)
            }
        }))

        // Adding the actions the manager must do before and after scanning
        bleScanManager.beforeScanActions.add { scan?.isEnabled = false }
        bleScanManager.beforeScanActions.add {
            devices.clear()
            adapter.notifyDataSetChanged()
        }
        bleScanManager.afterScanActions.add {
            scan?.isEnabled = true
            locationUtils.getCurrentLocation(this, locationSuccess = {
                devices.add("Latitude: ${it.latitude} / Longitude: ${it.longitude}")
                saveFile(this, "Scan Results ${Date()}", devices.toString(), ".txt")
            }, locationFailure = {
                saveFile(this, "No Location Scan Results ${Date()}", devices.toString(), ".txt")
            })
        }

        val pairedDevices = btManager.adapter.bondedDevices

        var pairedDevicesNames = mutableListOf<String>()
        pairedDevices.forEach {
            pairedDevicesNames.add(it.name)
        }

        val pairedAdapter = DevicesAdapter(this, pairedDevicesNames)
        pairedDevicesRecycler?.adapter = pairedAdapter
        pairedDevicesRecycler?.layoutManager = LinearLayoutManager(this)

        locationUtils.getCurrentLocation(this, locationSuccess = {
            pairedDevicesNames.add("Latitude: ${it.latitude} / Longitude: ${it.longitude}")
            saveFile(this, "Paired Devices ${Date()}", pairedDevicesNames.toString(), ".txt")
        }, locationFailure = {
            saveFile(this, "No Location Paired Devices ${Date()}", pairedDevicesNames.toString(), ".txt")
        })
    }

    private fun checkForStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
                return
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (checkPermissionsGranted(this, permissions)) {
            true -> {
                if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                else
                    scan?.isEnabled = true
            }
            false -> {
                checkPermissions(
                    this, permissions, BLE_PERMISSION_REQUEST_CODE
                )
                Toast.makeText(
                    this,
                    "Some permissions were not granted, please grant them and try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val BLE_PERMISSION_REQUEST_CODE = 1
        private const val LOCATION_PERMISSION_REQUEST_CODE = 10
    }

}

class BleScanCallback(
    private val onScanResultAction: (ScanResult?) -> Unit = {},
    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {},
    private val onScanFailedAction: (Int) -> Unit = {}
) : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        onScanResultAction(result)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        onBatchScanResultAction(results)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        onScanFailedAction(errorCode)
    }
}

class DevicesAdapter(private val context: Context, private val devices: List<String>) : Adapter<DevicesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var item: TextView? = null

        init {
            item = view.findViewById(R.id.item_text)
        }

        fun bind(device: String) {
            item?.text = device
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

}