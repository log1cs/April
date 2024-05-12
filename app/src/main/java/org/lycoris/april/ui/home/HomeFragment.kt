package org.lycoris.april.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import org.lycoris.april.databinding.FragmentHomeBinding
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var connectionOn = false // Initial connection state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val button1 = binding.button1
        button1.setOnClickListener {
            toggleConnection("26")
        }

        val button2 = binding.button2
        button2.setOnClickListener {
            toggleConnection("27")
        }

        val button3 = binding.button3
        button3.setOnClickListener {
            toggleConnection("25")
        }

        val button4 = binding.button4
        button4.setOnClickListener {
            toggleConnection("33")
        }


        val button5 = binding.button5
        button5.setOnClickListener {
            toggleConnection("32")
        }

        val button6 = binding.button6
        button6.setOnClickListener {
            toggleConnection("14")
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun toggleConnection(connect: String) {
        val targetUrl = if (connectionOn) {
            "http://192.168.4.1/$connect/off"
        } else {
            "http://192.168.4.1/$connect/on"
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(targetUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                Log.d(HomeFragment::class.java.simpleName, "Connecting to URL: $url")

                // Execute the HTTP request
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Toggle connection state
                    connectionOn = !connectionOn

                    // Update UI on the main thread if needed
                    withContext(Dispatchers.Main) {
                        // Handle UI updates here
                        // For example, update UI components based on connectionOn state
                        if (connectionOn) {
                            // Connection is now ON
                            // Update UI accordingly
                        } else {
                            // Connection is now OFF
                            // Update UI accordingly
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

