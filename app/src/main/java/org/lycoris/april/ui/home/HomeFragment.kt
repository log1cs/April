package org.lycoris.april.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothDeviceListActivity
import org.lycoris.april.bluetooth.BluetoothManager

class HomeFragment : Fragment() {

    private lateinit var bluetoothManager: BluetoothManager
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bluetoothManager = BluetoothManager(requireContext())

        // Button setup
        val button7: Button = view.findViewById(R.id.button7) // Đi Thẳng
        val button8: Button = view.findViewById(R.id.button8) // Rẽ Phải
        val button9: Button = view.findViewById(R.id.button9) // Rẽ Trái
        val button10: Button = view.findViewById(R.id.button10) // Đi Lùi

        // For demo: assume not connected
        val isConnected = false
        val grayColor = resources.getColor(android.R.color.darker_gray, null)
        val normalColor = resources.getColor(R.color.scarlet, null)

        val buttons = listOf(button7, button8, button9, button10)
        for (btn in buttons) {
            btn.isEnabled = isConnected
            btn.setBackgroundColor(if (isConnected) normalColor else grayColor)
        }

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

    private fun startBluetoothDeviceList() {
        val intent = Intent(requireContext(), BluetoothDeviceListActivity::class.java)
        startActivity(intent)
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