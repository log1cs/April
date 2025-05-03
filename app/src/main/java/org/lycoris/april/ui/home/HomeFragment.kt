package org.lycoris.april.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.lycoris.april.R
import org.lycoris.april.bluetooth.BluetoothManager

class HomeFragment : Fragment() {

    private lateinit var bluetoothManager: BluetoothManager
    private val deviceAddress = "XX:XX:XX:XX:XX:XX" // Replace with your device's MAC address

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        bluetoothManager = BluetoothManager(requireContext())

        // We have the permission, connect to the device
        bluetoothManager.connectToDevice(deviceAddress)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button1: Button = view.findViewById(R.id.button7)
        val button2: Button = view.findViewById(R.id.button8)
        val button3: Button = view.findViewById(R.id.button9)
        val button4: Button = view.findViewById(R.id.button10)
        /*val button5: Button = view.findViewById(R.id.button5)
        val button6: Button = view.findViewById(R.id.button6)*/

        button1.setOnClickListener {
            bluetoothManager.sendMessage("1")
        }
        button2.setOnClickListener {
            bluetoothManager.sendMessage("2")
        }
        button3.setOnClickListener {
            bluetoothManager.sendMessage("3")
        }
        button4.setOnClickListener {
            bluetoothManager.sendMessage("4")
        }
        /*button5.setOnClickListener {
            bluetoothManager.sendMessage("5")
        }
        button6.setOnClickListener {
            bluetoothManager.sendMessage("6")
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.disconnect()
    }
}