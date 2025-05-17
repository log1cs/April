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

class BluetoothManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    private val TAG: String = "BluetoothManager"
    private var connected = false

    init {
        val bluetoothManager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

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
            return
        }

        try {
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported")
                return
            }
            val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(uuid)

            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connected = true
            Log.d(TAG, "Connected to device")
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            connected = false
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
            // You should request the permission here, or handle the absence of permission
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
            // You should request the permission here, or handle the absence of permission
            return
        }

        try {
            bluetoothSocket?.close()
            connected = false
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }
}