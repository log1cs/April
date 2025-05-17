package org.lycoris.april.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothDeviceListActivity
import org.lycoris.april.bluetooth.BluetoothConnectionManager

class HomeFragment : Fragment() {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    private lateinit var textNoDeviceConnected: TextView
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var button9: Button
    private lateinit var button10: Button

    private val bluetoothDeviceListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Device was connected successfully
            updateUIState()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bluetoothManager = BluetoothConnectionManager.getInstance(requireContext())

        // Initialize views
        textNoDeviceConnected = view.findViewById(R.id.text_no_device_connected)
        button7 = view.findViewById(R.id.button7) // Đi Thẳng
        button8 = view.findViewById(R.id.button8) // Rẽ Phải
        button9 = view.findViewById(R.id.button9) // Rẽ Trái
        button10 = view.findViewById(R.id.button10) // Đi Lùi

        updateUIState()

        button7.setOnClickListener {
            bluetoothManager.sendMessage("1")
        }
        button8.setOnClickListener {
            bluetoothManager.sendMessage("2")
        }
        button9.setOnClickListener {
            bluetoothManager.sendMessage("3")
        }
        button10.setOnClickListener {
            bluetoothManager.sendMessage("4")
        }

        return view
    }

    private fun updateUIState() {
        val isConnected = bluetoothManager.isConnected()
        val normalColor = resources.getColor(R.color.scarlet, null)
        val grayColor = resources.getColor(android.R.color.darker_gray, null)

        // Update connection status text
        textNoDeviceConnected.text = if (isConnected) {
            "Connected to: ${bluetoothManager.getConnectedDeviceName()}"
        } else {
            "No device connected"
        }

        // Update button states
        val buttons = listOf(button7, button8, button9, button10)
        for (btn in buttons) {
            btn.isEnabled = isConnected
            btn.setBackgroundColor(if (isConnected) normalColor else grayColor)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUIState()
    }

    override fun onStart() {
        super.onStart()
        // Start observing connection state changes
        startObservingConnectionState()
    }

    override fun onStop() {
        super.onStop()
        // Stop observing connection state changes
        stopObservingConnectionState()
    }

    private fun startObservingConnectionState() {
        // Create a periodic check for connection state
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val checkConnection = object : Runnable {
            override fun run() {
                updateUIState()
                handler.postDelayed(this, 1000) // Check every second
            }
        }
        handler.post(checkConnection)
    }

    private fun stopObservingConnectionState() {
        // Remove any pending callbacks
        android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacksAndMessages(null)
    }

    private fun startBluetoothDeviceList() {
        val intent = Intent(requireContext(), BluetoothDeviceListActivity::class.java)
        bluetoothDeviceListLauncher.launch(intent)
    }

    private fun checkBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBluetoothPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            ),
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startBluetoothDeviceList()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Bluetooth permissions are required for this feature",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.disconnect()
    }
}