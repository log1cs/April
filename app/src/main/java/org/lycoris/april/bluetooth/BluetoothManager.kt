package org.lycoris.april.bluetooth
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import android.os.Handler
import android.os.Looper
//import kotlin.io.path.name

@SuppressLint("StaticFieldLeak")
class BluetoothConnectionManager private constructor(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    private val tag: String = "BluetoothConnectionManager"
    private var connected = false
    private var connectedDeviceName: String? = null

    // Renamed for clarity based on previous discussion, but functionality is similar
    private var repeatSendHandler = Handler(Looper.getMainLooper())
    private var repeatSendRunnable: Runnable? = null
    private val repeatSendDelayMS = 100L // Send every 100 milliseconds, adjust as needed

    /**
     * Hàm gửi tín hiệu liên tục.
     * Tất cả các tín hiệu vẫn đang trong hàng chờ cần phải
     * được dừng lại để tín hiệu mới thế chỗ vào.
     */
    fun startSendingRepeatedly(charToSend: Char) {
        // Dừng hết tất cả các tín hiệu trước đó
        stopSendingRepeatedly() 

        repeatSendRunnable = object : Runnable {
            override fun run() {
                // Nếu Robot đã được kết nối
                if (isConnected()) {
                    // Gửi tín hiệu lên Robot
                    sendMessage(charToSend)
                }
                // Gửi tín hiệu, với delay = repeatSendDelayMS (100)
                repeatSendHandler.postDelayed(this, repeatSendDelayMS)
            }
        }
        // Bắt đầu gửi liên tục
        repeatSendHandler.post(repeatSendRunnable!!)
        Log.d(tag, "Started repeatedly sending: $charToSend")
    }

    /**
     * Stops any currently active repeated sending of characters.
     * This function does NOT send any character upon stopping.
     */
    fun stopSendingRepeatedly() {
        repeatSendRunnable?.let {
            repeatSendHandler.removeCallbacks(it)
            Log.d(tag, "Stopped repeatedly sending.") // Log is fine
        }
        repeatSendRunnable = null
    }

    companion object {
        @Volatile
        private var INSTANCE: BluetoothConnectionManager? = null

        fun getInstance(context: Context): BluetoothConnectionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BluetoothConnectionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        val bluetoothManagerService: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManagerService.adapter
    }

    fun isConnected(): Boolean = connected

    fun getConnectedDeviceName(): String? = connectedDeviceName

    @Throws(IOException::class)
    fun connectToDevice(deviceAddress: String) {
        if (connected) {
            Log.w(tag, "Already connected to $connectedDeviceName")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "Missing BLUETOOTH_CONNECT permission. Cannot connect.")
            throw IOException("Missing Bluetooth permission (BLUETOOTH_CONNECT)")
        }

        if (bluetoothAdapter == null) {
            Log.e(tag, "Bluetooth not supported on this device.")
            throw IOException("Bluetooth not supported")
        }
        if (!bluetoothAdapter!!.isEnabled) {
            Log.e(tag, "Bluetooth is not enabled.")
            throw IOException("Bluetooth is not enabled")
        }

        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        if (bluetoothDevice == null) {
            Log.e(tag, "Device not found for address: $deviceAddress")
            throw IOException("Device not found: $deviceAddress")
        }

        try {
            // It's good practice to close any existing socket before creating a new one
            bluetoothSocket?.close()
            outputStream?.close()
        } catch (e: IOException) {
            Log.w(tag, "Error closing previous socket/stream before new connection: ${e.message}")
        }

        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)

        try {
            bluetoothSocket?.connect() // This is a blocking call
            outputStream = bluetoothSocket?.outputStream
            connected = true
            connectedDeviceName = bluetoothDevice.name ?: deviceAddress // Use BLUETOOTH_CONNECT for name
            Log.d(tag, "Successfully connected to device: $connectedDeviceName")
        } catch (connectException: IOException) {
            Log.e(tag, "Error connecting to device $deviceAddress: ${connectException.message}", connectException)
            // Attempt to close the socket on connection failure
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                Log.e(tag, "Error closing socket after connection failure: ${closeException.message}", closeException)
            }
            connected = false
            connectedDeviceName = null
            bluetoothSocket = null
            outputStream = null
            throw IOException("Error connecting to device: ${connectException.localizedMessage}", connectException)
        }
    }

    fun sendMessage(message: Char) {
        if (!connected || bluetoothSocket == null || outputStream == null) {
            Log.e(tag, "Cannot send message: Not connected or streams not ready.")
            // Optionally: could trigger a reconnect attempt or notify UI
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT // Still needed for operations on connected socket for some Android versions
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "Missing BLUETOOTH_CONNECT permission. Cannot send message.")
            return
        }
        try {
            outputStream?.write(message.code)
            // Log.d(TAG, "Sent char: $message (code: ${message.code})") // Kept original logging
        } catch (e: IOException) {
            Log.e(tag, "Error sending message: ${e.message}", e)
            disconnect() // Example: disconnect on send error
        }
    }

    fun disconnect() {
        stopSendingRepeatedly() // Ensure any repeated sending is stopped

        if (!connected && bluetoothSocket == null) {
            Log.w(tag, "Already disconnected or never connected.")
            return
        }

        try {
            outputStream?.close()
            bluetoothSocket?.close()
            Log.d(tag, "Disconnected from $connectedDeviceName.")
        } catch (e: IOException) {
            Log.e(tag, "Error disconnecting: ${e.message}", e)
        } finally {
            outputStream = null
            bluetoothSocket = null
            connected = false
            connectedDeviceName = null
        }
    }
}
