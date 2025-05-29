package org.lycoris.april.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent // Ensure this is present for MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.ui.input.pointer.isPressed
//import androidx.compose.ui.input.pointer.isPressed
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothDeviceListActivity
import org.lycoris.april.bluetooth.BluetoothConnectionManager

class HomeFragment : Fragment() {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    private lateinit var textNoDeviceConnected: TextView
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var button6: Button
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var button9: Button
    private lateinit var button10: Button
    private lateinit var button11: Button
    private lateinit var button12: Button

    private val bluetoothDeviceListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Device was connected successfully
            updateUIState()
        }
    }

    private fun createBluetoothTouchListener(messageOnPress: String, messageOnRelease: String? = null): View.OnTouchListener {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    bluetoothManager.sendMessage(messageOnPress)
                    v.isPressed = true
                    true
                }
                MotionEvent.ACTION_UP -> { // Handle ACTION_UP separately for performClick
                    // Only perform click if the touch up is within the view bounds
                    // (optional, but good practice for click behavior)
                    if (v.isPressed) { // Check if it was actually pressed (avoid issues if canceled mid-gesture)
                        messageOnRelease?.let { bluetoothManager.sendMessage(it) }
                        v.isPressed = false
                        v.performClick() // Call performClick for accessibility
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // If touch is canceled, we might not want to call performClick,
                    // but we should reset the pressed state and send release message.
                    messageOnRelease?.let { bluetoothManager.sendMessage(it) }
                    v.isPressed = false
                    true
                }
                else -> false
            }
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
        button2 = view.findViewById(R.id.button2) // Đi Thẳng Chéo Trái
        button3 = view.findViewById(R.id.button3) // Đi Thẳng Chéo Phải
        button4 = view.findViewById(R.id.button4) // Đi Lùi Chéo Trái
        button5 = view.findViewById(R.id.button5) // Đi Lùi Chéo Phải
        button6 = view.findViewById(R.id.button6) // Quay Vòng Trái
        button7 = view.findViewById(R.id.button7) // Đi Thẳng
        button8 = view.findViewById(R.id.button8) // Rẽ Phải
        button9 = view.findViewById(R.id.button9) // Rẽ Trái
        button10 = view.findViewById(R.id.button10) // Đi Lùi
        button11 = view.findViewById(R.id.button11) // Dừng Khẩn Cấp
        button12 = view.findViewById(R.id.button12) // Quay Vòng Phải

        updateUIState()

//        button7.setOnClickListener {
//            bluetoothManager.sendMessage("FS")
//        }
//        button8.setOnClickListener {
//            bluetoothManager.sendMessage("RS")
//        }
//        button9.setOnClickListener {
//            bluetoothManager.sendMessage("LS")
//        }
//        button10.setOnClickListener {
//            bluetoothManager.sendMessage("BS")
//        }
//        button2.setOnClickListener {
//            bluetoothManager.sendMessage("FL")
//        }
//        button3.setOnClickListener {
//            bluetoothManager.sendMessage("FR")
//        }
//        button4.setOnClickListener {
//            bluetoothManager.sendMessage("BL")
//        }
//        button5.setOnClickListener {
//            bluetoothManager.sendMessage("BR")
//        }
//        button11.setOnClickListener {
//            bluetoothManager.sendMessage("EM")
//        }
//        button6.setOnClickListener {
//            bluetoothManager.sendMessage("RL")
//        }
//        button12.setOnClickListener {
//            bluetoothManager.sendMessage("RR")
//        }
//
//        return view
        // --- Define your release message ---
        // This is the message sent when ANY of the movement buttons are released.
        // Adjust this if different buttons need different release messages or no message.
        val stopMessage = "ST" // Example: "ST" for STOP

        // Apply the OnTouchListener to your buttons
        button7.setOnTouchListener(createBluetoothTouchListener("FS", stopMessage)) // Đi Thẳng
        button8.setOnTouchListener(createBluetoothTouchListener("RS", stopMessage)) // Rẽ Phải
        button9.setOnTouchListener(createBluetoothTouchListener("LS", stopMessage)) // Rẽ Trái
        button10.setOnTouchListener(createBluetoothTouchListener("BS", stopMessage)) // Đi Lùi
        button2.setOnTouchListener(createBluetoothTouchListener("FL", stopMessage)) // Đi Thẳng Chéo Trái
        button3.setOnTouchListener(createBluetoothTouchListener("FR", stopMessage)) // Đi Thẳng Chéo Phải
        button4.setOnTouchListener(createBluetoothTouchListener("BL", stopMessage)) // Đi Lùi Chéo Trái
        button5.setOnTouchListener(createBluetoothTouchListener("BR", stopMessage)) // Đi Lùi Chéo Phải
        button6.setOnTouchListener(createBluetoothTouchListener("RL", stopMessage)) // Quay Vòng Trái
        button12.setOnTouchListener(createBluetoothTouchListener("RR", stopMessage)) // Quay Vòng Phải

        button11.setOnClickListener {
            bluetoothManager.sendMessage("EM")
       }

        return view
    }

    private fun updateUIState() {
        val isConnected = bluetoothManager.isConnected()
        val normalColor = resources.getColor(R.color.dark_blue, null)
        val emercyColor = resources.getColor(R.color.dark_red, null)
        val grayColor = resources.getColor(android.R.color.darker_gray, null)

        // Update connection status text
        textNoDeviceConnected.text = if (isConnected) {
            "Connected to: ${bluetoothManager.getConnectedDeviceName()}"
        } else {
            "No device connected"
        }

        // Update button states
        val buttons = listOf(button7, button8, button9, button10, button2, button3, button4, button5, button6, button12)
        for (btn in buttons) {
            btn.isEnabled = isConnected
            btn.setBackgroundColor(if (isConnected) normalColor else grayColor)
        }
        button11.isEnabled = isConnected
        button11.setBackgroundColor(if(isConnected) emercyColor else grayColor)
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