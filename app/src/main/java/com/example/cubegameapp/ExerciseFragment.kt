package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cubegameapp.databinding.FragmentExerciseBinding
import com.example.cubegameapp.model.ConnectState
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import splitties.toast.toast


class ExerciseFragment : Fragment() {

    private val TAG = "ExerciseFragment"

    private var _binding: FragmentExerciseBinding? = null
    private val viewModel: MainViewModel by activityViewModels()

    private var winner: String = ""
    private var scoreA: Int = 0
    private var scoreB: Int = 0
    private var scoreA1: Int = 0
    private var scoreB1: Int = 0
    private var gameOver = false
    var roundsVM: Int = 0
    var counterVM: Int = 0


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //---Gewinner wählen---
        binding.btnA.setOnClickListener {
            viewModel.incCounterA()
            viewModel.incBtnCounter()
            Log.i(TAG, "Runde: $viewModel.btnCounter")
            viewModel.counterA.observe(viewLifecycleOwner) {counterA ->
                scoreA1 = counterA
                toast(getString(R.string.scoreA).format(scoreA1))
            }
            /*val previousA = binding.tvNameA.text
            newA = listA.value?.random().toString()
            Log.i(TAG, "Button A previous A: $previousA")
            Log.i(TAG, "Button A new A: $newA")
            val previousB = binding.tvNameB.text
            newB = listB.value?.random().toString()
            Log.i(TAG, "Button A previous B: $previousB")
            Log.i(TAG, "Button A new B: $newB")
            if (previousA == newA || previousB == newB) {
                newA = listA.value?.random().toString()
                binding.tvNameA.text = newA
                binding.tvNameA.invalidate()
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }*/
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
            viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                counterVM = counter
                Log.i(TAG,"Counter: $counterVM")
            }
            viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                roundsVM = roundsSelected
                Log.i(TAG,"Rounds: $roundsVM")
            }
            var counter = counterVM - 1
            Log.i(TAG, "COUNTER Abgezogen: $counter")
            if (counter == roundsVM) {
                Log.i(TAG, "Status: $gameOver")
                checkRound()
            } else {
                //viewModel.switchBoolean()
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }

            //viewModel.sendDataPlay()
            //viewModel.sendDataStop()
            //showDialog()
        }

        binding.btnB.setOnClickListener {
            viewModel.incCounterB()
            viewModel.incBtnCounter()
            Log.i(TAG, "Runde: $viewModel.btnCounter")
            viewModel.counterB.observe(viewLifecycleOwner) {counterB ->
                scoreB1 = counterB
                toast(getString(R.string.scoreB).format(scoreB1))
            }
            /*val previousA = binding.tvNameA.text
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
            }*/
            /*if (previousB == newB) {
                newB = listB.value?.random().toString()
                binding.tvNameB.text = newB
            }*/
            viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                counterVM = counter
                Log.i(TAG,"Counter: $counterVM")
            }
            viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                roundsVM = roundsSelected
                Log.i(TAG,"Rounds: $roundsVM")
            }
            var counter = counterVM - 1
            Log.i(TAG, "COUNTER Abgezogen: $counter")
            if (counter == roundsVM) {
                Log.i(TAG, "Status: $gameOver")
                checkRound()
            } else {
                //viewModel.switchBoolean()
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }
            //viewModel.sendDataPlay()
            //viewModel.sendDataStop()
        }

        // Mittels Observer über Änderungen des connect status informieren
        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConnectState.NOT_CONNECTED -> {
                    toast("Bluetooth-Verbindung abgebrochen")
                    findNavController().navigate(R.id.action_exerciseFragment_to_FirstFragment)
                }
                ConnectState.NO_DEVICE -> {
                    toast("kein Bluetooth Gerät")
                    findNavController().navigate(R.id.action_exerciseFragment_to_FirstFragment)
                }
            }
        }
    }

    private fun checkRound() {
        //gameOver = true
        Log.i(TAG, "Spiel vorbei")
        viewModel.counterA.observe(viewLifecycleOwner){
                counterA -> scoreA = counterA
        }
        viewModel.counterB.observe(viewLifecycleOwner){
                counterB -> scoreB = counterB
        }
        if(scoreA > scoreB) {
            winner = "Team A"
        } else {
            winner = "Team B"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialogEnd))
            .setMessage(winner)
            .setPositiveButton(resources.getString(R.string.dialogEnter)) { dialog, which ->
                viewModel.sendDataStop()
                viewModel.switchBoolean()
                viewModel.resetCounterA()
                viewModel.resetCounterB()
                viewModel.resetBtnCounter()
                findNavController().navigate(R.id.action_exerciseFragment_to_FirstFragment)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //viewModel.sendDataStop()
    }
}