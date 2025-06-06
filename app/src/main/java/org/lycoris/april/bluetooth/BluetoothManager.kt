package org.lycoris.april.bluetooth

import android.Manifest
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

class BluetoothConnectionManager private constructor(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    private val TAG: String = "BluetoothConnectionManager"
    private var connected = false
    private var connectedDeviceName: String? = null

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
        val bluetoothManager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun isConnected(): Boolean = connected

    fun getConnectedDeviceName(): String? = connectedDeviceName

    @Throws(IOException::class)
    fun connectToDevice(deviceAddress: String) {
        if (connected) {
            Log.w(TAG, "Already connected")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing Bluetooth permission. Perhaps you forgot to allow it?")
            throw IOException("Missing Bluetooth permission")
        }

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported")
            throw IOException("Bluetooth not supported")
        }
        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)

        try {
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connected = true
            connectedDeviceName = bluetoothDevice?.name ?: deviceAddress
            Log.d(TAG, "Connected to device")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            connected = false
            connectedDeviceName = null
            throw IOException("Error connecting to device: ${e.localizedMessage}", e)
        }
    }

    fun sendMessage(message: String) {
        if (!connected) {
            Log.e(TAG, "Error not connected to device")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission")
            return
        }
        try {
            if (outputStream != null) {
                val bytes = message.toByteArray()
                outputStream?.write(bytes)
                Log.d(TAG, "Message sent: $message")
            } else {
                Log.e(TAG, "Error outputStream is null")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error sending message", e)
        }
    }

    fun disconnect() {
        if (!connected) {
            Log.w(TAG, "Already disconnected")
            return
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission")
            return
        }

        try {
            bluetoothSocket?.close()
            connected = false
            connectedDeviceName = null
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }
}