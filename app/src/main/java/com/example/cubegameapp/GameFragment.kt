package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cubegameapp.databinding.FragmentGameBinding
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class GameFragment : Fragment() {

    private val TAG = "GameFragment"

    private var _binding: FragmentGameBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    private var useSelected: String = ""

    val singleItems = arrayOf("3 Runden", "5 Runden", "10 Runden")
    var checkedItem = 1

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //---Verwendungszweck empfangen---
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")


        //---Rundenanzahl wählen---
        var selectedItem = singleItems[checkedItem]

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialogTitle))
            .setPositiveButton(resources.getString(R.string.dialogEnter)) { dialog, which ->
                binding.tvRunden.text = selectedItem
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                checkedItem = which
                selectedItem = singleItems[which]
            }
            .show()

        val scope = MainScope()
        scope.launch {
            delay(3000)
            Log.i(TAG, "delay")
            viewModel.sendData()
            viewModel.startDataLoadJob()
        }


        viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            binding.tvData.text = "${data.playStatus}\n${data.seite}"
            // process data ....
        }

        binding.btnRepeat.setOnClickListener {
            viewModel.sendDataRepeat()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.cancelDataLoadJob()
    }
}