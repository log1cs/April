package org.lycoris.april.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.lycoris.april.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.lycoris.april.databinding.FragmentHomeBinding
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        val constraintLayout = view?.findViewById<ConstraintLayout>(R.id.text_home) // Make sure you have a ConstraintLayout with an id
        constraintLayout?.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.bgz))

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}