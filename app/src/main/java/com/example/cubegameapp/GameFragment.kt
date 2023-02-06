package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cubegameapp.databinding.FragmentGameBinding
import com.example.cubegameapp.model.ConnectState
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

    val singleItems = arrayOf("3","5","10")
    var checkedItem = 1
    var nrRounds : Int = 0
    var rounds = 0

    private var scoreA: Int = 0
    private var scoreB: Int = 0
    private var newA: String = ""
    private var newB: String = ""
    private var winner: String = ""
    private var gameOver = false
    private var prevSeite: String = ""


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
                if (selectedItem == "3") {
                    nrRounds = 3
                    Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                }
                if (selectedItem == "5") {
                    nrRounds = 5
                    Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                }
                if (selectedItem == "10") {
                    nrRounds = 10
                    Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                }
                nrRounds = selectedItem.toInt()
                Log.i(TAG,"Rundennummer: $nrRounds")
            }
            // Single-choice items (initialized with checked item)
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                checkedItem = which
                selectedItem = singleItems[which]
            }
            .show()



        //---Daten senden und empfangen---
        val scope = MainScope()
        scope.launch {
            delay(3000)
            Log.i(TAG, "delay")
            //viewModel.sendDataPlay()
            //viewModel.startDataLoadJob()
        }


        //---Name Team A---
        val listA = viewModel.selectedPlayerListA
        binding.tvNameA.text = listA.value?.random().toString()


        //---Name Team B---
        val listB = viewModel.selectedPlayerListB
        binding.tvNameB.text = listB.value?.random().toString()


        //---empfangene Daten---
        viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            //binding.tvData.text = "${data.playStatus}\n${data.seite}"
            if (data.seite == prevSeite) {
                toast("Nochmal würfeln")
            } else {
                binding.tvData.text = "${data.seite}"
                if (data.seite == "1") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = true
                    binding.iv2.isVisible = false
                    binding.iv3.isVisible = false
                    binding.iv4.isVisible = false
                    binding.iv5.isVisible = false
                    binding.iv6.isVisible = false
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
                if (data.seite == "2") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = false
                    binding.iv2.isVisible = true
                    binding.iv3.isVisible = false
                    binding.iv4.isVisible = false
                    binding.iv5.isVisible = false
                    binding.iv6.isVisible = false
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
                if (data.seite == "3") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = false
                    binding.iv2.isVisible = false
                    binding.iv3.isVisible = true
                    binding.iv4.isVisible = false
                    binding.iv5.isVisible = false
                    binding.iv6.isVisible = false
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
                if (data.seite == "4") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = false
                    binding.iv2.isVisible = false
                    binding.iv3.isVisible = false
                    binding.iv4.isVisible = true
                    binding.iv5.isVisible = false
                    binding.iv6.isVisible = false
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
                if (data.seite == "5") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = false
                    binding.iv2.isVisible = false
                    binding.iv3.isVisible = false
                    binding.iv4.isVisible = false
                    binding.iv5.isVisible = true
                    binding.iv6.isVisible = false
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
                if (data.seite == "6") {
                    //viewModel.sendDataStop()
                    viewModel.cancelDataLoadJob()
                    binding.iv1.isVisible = false
                    binding.iv2.isVisible = false
                    binding.iv3.isVisible = false
                    binding.iv4.isVisible = false
                    binding.iv5.isVisible = false
                    binding.iv6.isVisible = true
                    prevSeite = data.seite
                    Log.i(TAG, "Previous: $prevSeite")
                }
            }
        }


        //---Wiederholen---
        binding.btnDice.setOnClickListener {
            viewModel.startDataLoadJob()
            viewModel.sendDataRepeat()
            viewModel.sendDataPlay()
        }


        //---Gewinner wählen---
        binding.btnA.setOnClickListener {
            scoreA++
            rounds++
            Log.i(TAG, "Runde: $rounds")
            toast(getString(R.string.scoreA).format(scoreA))

            val previousA = binding.tvNameA.text
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
            if (rounds == nrRounds) {
                Log.i(TAG, "Status: $gameOver")
                checkRound()
            }
            //viewModel.sendDataPlay()
            viewModel.sendDataStop()
        }

        binding.btnB.setOnClickListener {
            scoreB++
            rounds++
            Log.i(TAG, "Runde: $rounds")
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
            if (rounds == nrRounds) {
                Log.i(TAG, "Status: $gameOver")
                checkRound()
            }
            //viewModel.sendDataPlay()
            viewModel.sendDataStop()
        }

        // Mittels Observer über Änderungen des connect status informieren
        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConnectState.NOT_CONNECTED -> {
                    toast("Bluetooth-Verbindung abgebrochen")
                    findNavController().navigate(R.id.action_gameFragment_to_FirstFragment)
                }
                ConnectState.NO_DEVICE -> {
                    toast("kein Bluetooth Gerät")
                    findNavController().navigate(R.id.action_gameFragment_to_FirstFragment)
                }
            }
        }
    }


    private fun checkRound() {
            //gameOver = true
            Log.i(TAG, "Spiel vorbei")
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
                    findNavController().navigate(R.id.action_gameFragment_to_FirstFragment)
                }
                .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.sendDataStop()
        //viewModel.cancelDataLoadJob()
    }
}