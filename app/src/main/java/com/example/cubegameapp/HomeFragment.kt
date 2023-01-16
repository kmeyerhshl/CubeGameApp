package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cubegameapp.databinding.FragmentHomeBinding
import com.example.cubegameapp.model.ConnectState
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private val TAG = "Home"

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private var useSelected = ""
    private val button1 : String = "Button1"
    private val button2: String = "Button2"
    private val button3: String = "Button3"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startScan()
        val scope = MainScope()
        scope.launch {
            delay(1000)
            Log.i("ButtonFirst", "[btnStart] isReceivingData - true")
            viewModel.connect()
        }

        binding.textviewFirst.text = viewModel.getDeviceSelected()


        binding.button1.setOnClickListener {
            Log.i(TAG, "Button1")
            useSelected = button1
            viewModel.setUseSelected(useSelected)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.button2.setOnClickListener {
            Log.i(TAG, "Button2")
            useSelected = button2
            viewModel.setUseSelected(useSelected)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.button3.setOnClickListener {
            Log.i(TAG, "Button3")
            useSelected = button3
            viewModel.setUseSelected(useSelected)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.btnAnleitung.setOnClickListener {
            showDialog()
        }


        // Mittels Observer über Änderungen des connect status informieren
        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConnectState.CONNECTED -> {
                    binding.tvConnection.text = getString(R.string.connected)
                }
                ConnectState.NOT_CONNECTED -> {
                    binding.tvConnection.text = getString(R.string.disconnected)
                }
                ConnectState.NO_DEVICE -> {
                    binding.tvConnection.text = getString(R.string.no_selected_device)
                }
                ConnectState.DEVICE_SELECTED -> {
                    binding.tvConnection.text = getString(R.string.connecting)
                }
            }
        }
    }

    private fun showDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.fabAnleitung)
                .setMessage(R.string.anleitung)
                .setPositiveButton(R.string.dialog_cancel) { dialog, which ->
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}