package org.lycoris.april.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler // Correct import for Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.ui.input.pointer.isPressed
//import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothDeviceListActivity
import org.lycoris.april.bluetooth.BluetoothConnectionManager

@SuppressLint("ClickableViewAccessibility")
class HomeFragment : Fragment() {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    //private val REQUEST_BLUETOOTH_PERMISSIONS = 1001 // Keep this if you handle permissions here

    private lateinit var textNoDeviceConnected: TextView
    private lateinit var imageView2: ImageView

    // Declare all your buttons
    private lateinit var buttonFwd: Button      // Was button7 (Đi Thẳng - F)
    private lateinit var buttonBack: Button     // Was button10 (Đi Lùi - B)
    private lateinit var buttonLeft: Button     // Was button9 (Rẽ Trái - L)
    private lateinit var buttonRight: Button    // Was button8 (Rẽ Phải - R)

    private lateinit var buttonFwdLeft: Button  // Was button2 (Đi Thẳng Chéo Trái - A)
    private lateinit var buttonFwdRight: Button // Was button3 (Đi Thẳng Chéo Phải - I)
    private lateinit var buttonBackLeft: Button // Was button4 (Đi Lùi Chéo Trái - C)
    private lateinit var buttonBackRight: Button// Was button5 (Đi Lùi Chéo Phải - D)

    private lateinit var buttonRotateLeft: Button // Was button6 (Quay Vòng Trái - G)
    private lateinit var buttonRotateRight: Button// Was button12 (Quay Vòng Phải - H)

    private lateinit var buttonEmergencyStop: Button // Was button11 (Dừng Khẩn Cấp - M)

    private val stopMessageChar: Char = 'S'
    private val tagHomeFragment = "HomeFragment" // For logging

    // For observing connection state changes reliably
    private var wasBluetoothConnected = false
    private var connectionCheckHandler: Handler = Handler(Looper.getMainLooper())
    private var connectionCheckRunnable: Runnable? = null

    private val bluetoothDeviceListLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // updateUIState will be called by the connection observer,
            // which will also handle sending 'S' if newly connected.
            Log.d(tagHomeFragment, "Returned from BluetoothDeviceListActivity with RESULT_OK")
        }
    }

    private fun createBluetoothTouchListener(
        messageOnPress: Char,
        rotationAngleOnPress: Float? = null
    ): View.OnTouchListener {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (bluetoothManager.isConnected()) {
                        bluetoothManager.startSendingRepeatedly(messageOnPress)
                        Log.d(tagHomeFragment, "ACTION_DOWN: Starting to send '$messageOnPress'")
                        rotationAngleOnPress?.let {
                            imageView2.animate().rotation(it).setDuration(150).start()
                        }
                        v.isPressed = true
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (v.isPressed) {
                        if (bluetoothManager.isConnected()) {
                            bluetoothManager.startSendingRepeatedly(stopMessageChar)
                            Log.d(tagHomeFragment, "ACTION_UP: Starting to send '$stopMessageChar'")
                        }
                        imageView2.animate().rotation(0f).setDuration(150).start()
                        v.isPressed = false
                        v.performClick()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (v.isPressed) {
                        if (bluetoothManager.isConnected()) {
                            bluetoothManager.startSendingRepeatedly(stopMessageChar)
                            Log.d(tagHomeFragment, "ACTION_CANCEL: Starting to send '$stopMessageChar'")
                        }
                        imageView2.animate().rotation(0f).setDuration(150).start()
                        v.isPressed = false
                    }
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
        Log.d(tagHomeFragment, "onCreateView called")

        bluetoothManager = BluetoothConnectionManager.getInstance(requireContext())

        textNoDeviceConnected = view.findViewById(R.id.text_no_device_connected)
        imageView2 = view.findViewById(R.id.imageView2)

        // Initialize buttons (using your R.id names)
        buttonFwd = view.findViewById(R.id.button7)
        buttonFwdRight = view.findViewById(R.id.button3)
        buttonRight = view.findViewById(R.id.button8)
        buttonBackRight = view.findViewById(R.id.button5)
        buttonBack = view.findViewById(R.id.button10)
        buttonBackLeft = view.findViewById(R.id.button4)
        buttonLeft = view.findViewById(R.id.button9)
        buttonFwdLeft = view.findViewById(R.id.button2)
        buttonRotateLeft = view.findViewById(R.id.button6)
        buttonRotateRight = view.findViewById(R.id.button12)
        buttonEmergencyStop = view.findViewById(R.id.button11)

	// Góc bắt đầu của icon xe trên ứng dụng
        imageView2.rotation = 0f 

        // Cài đặt hàm di chuyển cho Robot
        // Cú pháp: Pair(Kí tự Bluetooth được gán trên Arduino, Góc quay)
        val movementButtonActions = mapOf(
            buttonFwd to Pair('F', 0f),			 // Đi tiến thẳng
            buttonFwdRight to Pair('I', 45f),    // Đi tiến chéo phải
            buttonRight to Pair('R', 90f),		 // Đi sang phải
            buttonBackRight to Pair('D', 135f),  // Đi lùi chéo phải
            buttonBack to Pair('B', 180f),		 // Đi lùi
            buttonBackLeft to Pair('C', 225f),	 // Đi lùi chéo trái
            buttonLeft to Pair('L', 270f),		 // Đi sang trái
            buttonFwdLeft to Pair('A', 315f),	 // Đi tiến chéo trái
            buttonRotateLeft to Pair('G', 270f), // Xoay trái
            buttonRotateRight to Pair('H', 90f)  // Xoay phải
        )

        for ((button, action) in movementButtonActions) {
            button.setOnTouchListener(
                createBluetoothTouchListener(action.first, action.second)
            )
        }

        // Nút dừng khẩn cấp
        buttonEmergencyStop.setOnClickListener {
	    // Nếu Robot đã được kết nối, tiếp tục
            if (bluetoothManager.isConnected()) {
		// Dừng tất cả các lệnh hiện tại đang chờ để được gửi đi
                bluetoothManager.stopSendingRepeatedly()
		// Gửi kí hiệu 'M' (dừng lại)
                bluetoothManager.sendMessage('M')        
                Log.d(tagHomeFragment, "Emergency Stop: Stopped all repeat, sent 'M'")
                imageView2.animate().rotation(0f).setDuration(150).start()
            } else {
		// Trong trường hợp chưa kết nối được đến robot mà người dùng bấm nút dừng khẩn cấp
                Log.d(tagHomeFragment, "Emergency Stop: Clicked but not connected.")
            }
        }

        // Example: Button to open Bluetooth device list (if you have one, e.g. on textNoDeviceConnected)
        textNoDeviceConnected.setOnClickListener {
            // Check for permissions before starting activity
            if (hasBluetoothPermissions()) {
                startBluetoothDeviceList()
            } else {
                requestBluetoothPermissions()
            }
        }
        return view
    }

    // Cập nhật UI
    private fun updateUIState() {
	// Kiểm tra xem thiết bị đã được kết nối chưa bằng cách gọi bluetoothManager.isConnected()
        val isConnected = bluetoothManager.isConnected()
        Log.d(tagHomeFragment, "updateUIState - isConnected: $isConnected")

	// Gán màu cho các biến
        val normalColor = ContextCompat.getColor(requireContext(), R.color.dark_blue)
        val emergencyColor = ContextCompat.getColor(requireContext(), R.color.dark_red)
        val grayColor = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

	// Kiểm tra xem thiết bị đã được kết nối hay chưa
        textNoDeviceConnected.text = if (isConnected) {
            "Connected to: ${bluetoothManager.getConnectedDeviceName() ?: "Unknown Device"}"
        } else {
            "No device connected (Tap to connect)"
        }

	// Gán hết các nút thành 1 danh sách
        val allMovementButtons = listOf(
            buttonFwd, buttonFwdRight, buttonRight, buttonBackRight, buttonBack,
            buttonBackLeft, buttonLeft, buttonFwdLeft, buttonRotateLeft, buttonRotateRight
        )
	
	// Sau đó, nếu Robot đã được kết nối, sẽ thay hết các nút thành màu xanh.
	// Nếu không, giữ màu xám.
        for (btn in allMovementButtons) {
            btn.isEnabled = isConnected
            btn.setBackgroundColor(if (isConnected) normalColor else grayColor)
        }

	// Sau đó, nếu Robot đã được kết nối, sẽ thay nút dừng khẩn cấp thành màu đỏ.
	// Nếu không, giữ màu xám.
        buttonEmergencyStop.isEnabled = isConnected
        buttonEmergencyStop.setBackgroundColor(if (isConnected) emergencyColor else grayColor)

        if (!isConnected && ::imageView2.isInitialized) {
            imageView2.rotation = 0f
        }
        // wasBluetoothConnected will be updated by the observer
    }

    override fun onResume() {
        super.onResume()
        Log.d(tagHomeFragment, "onResume called")
        // Initial state update and start observer if not already running
        wasBluetoothConnected = bluetoothManager.isConnected() // Sync initial state
        updateUIState()
        startObservingConnectionState() // Start or ensure observer is running
    }

    override fun onPause() {
        super.onPause()
        Log.d(tagHomeFragment, "onPause called")
        // Stop the observer when the fragment is not active to save resources
        // It will be restarted in onResume
        stopObservingConnectionState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tagHomeFragment, "onDestroyView called")
        // It's good practice to stop observers and handlers tied to the view here
        stopObservingConnectionState()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tagHomeFragment, "onDestroy called")
        if (::bluetoothManager.isInitialized) {
            bluetoothManager.stopSendingRepeatedly()
            bluetoothManager.disconnect()
            Log.d(tagHomeFragment, "onDestroy: Stopped all repeat, disconnected.")
        }
        // Ensure handler is definitely cleaned up
        connectionCheckHandler.removeCallbacksAndMessages(null)
    }

    private fun startObservingConnectionState() {
        // Stop any existing runnable before starting a new one to avoid multiple observers
        connectionCheckRunnable?.let { connectionCheckHandler.removeCallbacks(it) }

        connectionCheckRunnable = object : Runnable {
            override fun run() {
                val isCurrentlyConnected = bluetoothManager.isConnected()
                // Update UI regardless of state change first
                // This will also update textNoDeviceConnected
                updateUIState()

                if (isCurrentlyConnected != wasBluetoothConnected) {
                    Log.d(tagHomeFragment, "Connection Observer: State changed. Was: $wasBluetoothConnected, Is: $isCurrentlyConnected")
                    if (isCurrentlyConnected) {
                        // Just connected
                        Log.d(tagHomeFragment, "Connection Observer: Device connected, starting to send '$stopMessageChar'")
                        bluetoothManager.startSendingRepeatedly(stopMessageChar)
                    } else {
                        // Just disconnected
                        Log.d(tagHomeFragment, "Connection Observer: Device disconnected, stopping all repeat sends.")
                        bluetoothManager.stopSendingRepeatedly()
                        imageView2.rotation = 0f // Reset arrow on disconnect
                    }
                    wasBluetoothConnected = isCurrentlyConnected // Update the state
                }
                connectionCheckHandler.postDelayed(this, 1000) // Check every 1 second
            }
        }
        // Post immediately to check current state and then schedule
        connectionCheckHandler.post(connectionCheckRunnable!!)
        Log.d(tagHomeFragment, "Connection observer started.")
    }

    private fun stopObservingConnectionState() {
        connectionCheckRunnable?.let {
            connectionCheckHandler.removeCallbacks(it)
            Log.d(tagHomeFragment, "Connection observer stopped.")
        }
        connectionCheckRunnable = null // Allow it to be GC'd
    }

    // --- Bluetooth Permission Handling (Example) ---
    private fun hasBluetoothPermissions(): Boolean {
        // For Android 12 (API 31) and above
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else { // For older versions
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED // Often needed for scan
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) allGranted = false
            }
            if (allGranted) {
                Log.d(tagHomeFragment, "All Bluetooth permissions granted.")
                startBluetoothDeviceList()
            } else {
                Log.w(tagHomeFragment, "Not all Bluetooth permissions were granted.")
                Toast.makeText(requireContext(), "Bluetooth permissions are required to connect.", Toast.LENGTH_LONG).show()
            }
        }

    private fun requestBluetoothPermissions() {
        val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION // Request location for older BT scan
            )
        }
        requestPermissionsLauncher.launch(permissionsToRequest)
    }

    private fun startBluetoothDeviceList() {
        Log.d(tagHomeFragment, "Launching BluetoothDeviceListActivity.")
        val intent = Intent(requireContext(), BluetoothDeviceListActivity::class.java)
        bluetoothDeviceListLauncher.launch(intent)
    }
}
