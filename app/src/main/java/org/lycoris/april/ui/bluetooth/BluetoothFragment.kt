package org.lycoris.april.ui.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothManager
import org.lycoris.april.bluetooth.BluetoothDeviceAdapter

class BluetoothFragment : Fragment() {
    private val TAG = "BluetoothFragment"
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1001

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var btManager: BluetoothManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BluetoothDeviceAdapter
    private val deviceList = mutableListOf<BluetoothDevice>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: android.content.Intent?) {
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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        btManager = BluetoothManager(requireContext())
        setupRecyclerView(view)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bluetooth, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan -> {
                if (checkPermissions()) {
                    startDiscovery()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewDevices)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = BluetoothDeviceAdapter(deviceList) { device ->
            connectToDevice(device)
        }
        recyclerView.adapter = adapter
    }

    private fun connectToDevice(device: BluetoothDevice) {
        try {
            btManager.connectToDevice(device.address)
            Toast.makeText(requireContext(), "Connected to: ${device.name ?: device.address}", Toast.LENGTH_SHORT).show()
        } catch (e: java.io.IOException) {
            Toast.makeText(requireContext(), "Failed to connect: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Connection failed", e)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Unexpected error", e)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
            false
        } else {
            true
        }
    }

    private fun startDiscovery() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
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
        requireContext().registerReceiver(receiver, filter)
        if (bluetoothAdapter.startDiscovery()) {
            Toast.makeText(requireContext(), "Scanning for devices...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to start discovery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                Toast.makeText(requireContext(), "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireContext().unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Receiver not registered or already unregistered")
        }
        bluetoothAdapter.cancelDiscovery()
    }
} 