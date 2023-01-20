package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.cubegameapp.databinding.FragmentGameBinding
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.toast.toast


class GameFragment : Fragment() {

    private val TAG = "GameFragment"

    private var _binding: FragmentGameBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    private var useSelected: String = ""

    val singleItems = arrayOf("3", "5", "10")
    var checkedItem = 1

    private var scoreA: Int = 0
    private var scoreB: Int = 0
    private var newA: String = ""
    private var newB: String = ""


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
                viewModel.sendRoundData(selectedItem)
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                checkedItem = which
                selectedItem = singleItems[which]
            }
            .show()


        //---Daten senden und empfangen---
        /*val scope = MainScope()
        scope.launch {
            delay(3000)
            Log.i(TAG, "delay")
            viewModel.sendData()
            viewModel.startDataLoadJob()
        }*/


        //---Name Team A---
        val listA = viewModel.selectedPlayerListA
        binding.tvNameA.text = listA.value?.random().toString()


        //---Name Team B---
        val listB = viewModel.selectedPlayerListB
        binding.tvNameB.text = listB.value?.random().toString()


        //---empfangene Daten---
        viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            binding.tvData.text = "${data.playStatus}\n${data.seite}"
            // process data ....
        }


        //---Wiederholen---
        binding.btnRepeat.setOnClickListener {
            viewModel.sendDataRepeat()
        }


        //---Gewinner wählen---
        binding.btnA.setOnClickListener {
            scoreA++
            toast(getString(R.string.scoreA).format(scoreA))
            val previousA = binding.tvNameA.text
            newA = listA.value?.random().toString()
            Log.i(TAG, "Button A previous A: $previousA")
            Log.i(TAG, "Button A new A: $newA")
            val previousB = binding.tvNameB.text
            newB = listB.value?.random().toString()
            Log.i(TAG, "Button A previous B: $previousB")
            Log.i(TAG, "Button A new B: $newB")
            if (previousA == newA && previousB == newB) {
                newA = listA.value?.random().toString()
                binding.tvNameA.text = newA
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }
            /*val previousA = binding.tvNameA.text
            var newA = listA.value?.random().toString()
            if (previousA == newA) {
                newA = listA.value?.random().toString()
                binding.tvNameA.text = newA
            }
            val previousB = binding.tvNameB.text
            var newB = listB.value?.random().toString()
            if (previousB == newB) {
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }*/
        }

        binding.btnB.setOnClickListener {
            scoreB++
            toast(getString(R.string.scoreB).format(scoreB))
            val previousA = binding.tvNameA.text
            newA = listA.value?.random().toString()
            Log.i(TAG, "Button B previous A: $previousA")
            Log.i(TAG, "Button B new A: $newA")
            val previousB = binding.tvNameB.text
            newB = listB.value?.random().toString()
            Log.i(TAG, "Button B previous B: $previousB")
            Log.i(TAG, "Button B new B: $newB")
            if (previousA == newA && previousB == newB) {
                newA = listA.value?.random().toString()
                binding.tvNameA.text = newA
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }
            /*if (previousB == newB) {
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }*/
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.cancelDataLoadJob()
    }
}