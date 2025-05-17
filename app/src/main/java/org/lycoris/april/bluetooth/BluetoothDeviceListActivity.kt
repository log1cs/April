package org.lycoris.april.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.lycoris.april.R

class BluetoothDeviceListActivity : AppCompatActivity() {

    private val TAG = "BluetoothDeviceList"
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1001

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var txtConnectedDevice: TextView
    private lateinit var btManager: BluetoothManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BluetoothDeviceAdapter
    private val deviceList = mutableListOf<BluetoothDevice>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!deviceList.contains(it)) {
                        deviceList.add(it)
                        adapter.notifyDataSetChanged()
                        Log.d(TAG, "Found device: ${it.name ?: it.address}")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device_list)
        Log.d(TAG, "Activity created")

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        txtConnectedDevice = findViewById(R.id.txtConnectedDevice)
        btManager = BluetoothManager(this)

        setupRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "Creating options menu")
        menuInflater.inflate(R.menu.menu_bluetooth, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Menu item selected: ${item.itemId}")
        return when (item.itemId) {
            R.id.action_scan -> {
                Log.d(TAG, "Scan action selected")
                if (checkPermissions()) {
                    startDiscovery()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewDevices)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BluetoothDeviceAdapter(deviceList) { device ->
            connectToDevice(device)
        }
        recyclerView.adapter = adapter
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Connecting to device: ${device.name ?: device.address}")
        txtConnectedDevice.text = "Connected to: ${device.name ?: device.address}"
        btManager.connectToDevice(device.address)
    }

    private fun checkPermissions(): Boolean {
        Log.d(TAG, "Checking permissions")
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return if (permissions.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: $permissions")
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
            false
        } else {
            Log.d(TAG, "All permissions granted")
            true
        }
    }

    private fun startDiscovery() {
        Log.d(TAG, "Starting discovery")
        if (!bluetoothAdapter.isEnabled) {
            Log.d(TAG, "Bluetooth is not enabled")
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothAdapter.cancelDiscovery()
        deviceList.clear()

        // Add paired devices first
        val pairedDevices = bluetoothAdapter.bondedDevices
        for (device in pairedDevices) {
            if (!deviceList.contains(device)) {
                deviceList.add(device)
            }
        }
        adapter.notifyDataSetChanged()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        if (bluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "Discovery started successfully")
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "Failed to start discovery")
            Toast.makeText(this, "Failed to start discovery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "Permission result received: $requestCode")
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All permissions granted, starting discovery")
                startDiscovery()
            } else {
                Log.e(TAG, "Permissions denied")
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Receiver not registered or already unregistered")
        }
        bluetoothAdapter.cancelDiscovery()
    }
}
