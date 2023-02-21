package com.example.cubegameapp

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.toast.toast


class GameFragment : Fragment() {

    private val TAG = "GameFragment"

    private var _binding: FragmentGameBinding? = null

    private val viewModel: MainViewModel by activityViewModels()

    private var useSelected: String = ""

    //Variablen zur Rundenauswahl
    val singleItems = arrayOf("3","5","10")
    var checkedItem = 1
    var nrRounds : Int = 0
    var roundsVM: Int = 0
    var counterVM: Int = 0
    var bool1: Boolean = false


    private var newA: String = ""
    private var newB: String = ""
    private var prevSeite: String = ""

    //Variablen für Datenbank
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //---Verwendungszweck empfangen---
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")


        viewModel.booleanNext.observe(viewLifecycleOwner) {bool2 ->
            if (!bool2) {
                viewModel.switchBoolean()
                //---Rundenanzahl wählen---
                var selectedItem = singleItems[checkedItem]
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.dialogTitle))
                    .setPositiveButton(resources.getString(R.string.dialogEnter)) { dialog, which ->
                        //binding.tvRunden.text = selectedItem
                        viewModel.sendRoundData(selectedItem.toInt())
                        //viewModel.setRoundsSelected(selectedItem)
                        if (selectedItem == "3") {
                            nrRounds = 3
                            viewModel.setRoundsSelected(nrRounds)
                            Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                        }
                        if (selectedItem == "5") {
                            nrRounds = 5
                            viewModel.setRoundsSelected(nrRounds)
                            Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                        }
                        if (selectedItem == "10") {
                            nrRounds = 10
                            viewModel.setRoundsSelected(nrRounds)
                            Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                        }
                        nrRounds = selectedItem.toInt()
                        Log.i(TAG,"Rundennummer: $nrRounds")
                        //viewModel.sendDataPlay()
                        viewModel.gameStatus.gameStatus = "P"
                        viewModel.sendGameStatus()
                        viewModel.startDataLoadJob()

                        viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                            counterVM = counter
                            Log.i(TAG,"Counter: $counterVM")
                        }
                        viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                            roundsVM = roundsSelected
                            Log.i(TAG,"Rounds: $roundsVM")
                        }
                        binding.tvRunden.text = "Runde: $counterVM / $roundsVM"
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                        checkedItem = which
                        selectedItem = singleItems[which]
                    }
                    .show()
            } else {
                Log.i(TAG, "Rundenanzahl bereits ausgewählt")
                viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                    counterVM = counter
                    Log.i(TAG,"Counter: $counterVM")
                }
                viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                    roundsVM = roundsSelected
                    Log.i(TAG,"Rounds: $roundsVM")
                }
                binding.tvRunden.text = "Runde: $counterVM / $roundsVM"
            }
        }


        /*viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
            counterVM = counter
            Log.i(TAG,"Counter: $counterVM")
        }
        viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
            roundsVM = roundsSelected
            Log.i(TAG,"Rounds: $roundsVM")
        }
        binding.tvRunden.text = "Runde: $counterVM / $roundsVM"*/
        //binding.tvRunden.text = "Runde: ${viewModel.btnCounter} / ${viewModel.getRoundsSelected()}"


        //---Daten senden und empfangen---
        /*val scope = MainScope()
        scope.launch {
            delay(3000)
            Log.i(TAG, "delay")
            //viewModel.sendDataPlay()
            //viewModel.startDataLoadJob()
        }*/


        //---Name Team A---
        val listA = viewModel.selectedPlayerListA
        binding.tvNameA.text = listA.value?.random().toString()

        //---Name Team B---
        val listB = viewModel.selectedPlayerListB
        binding.tvNameB.text = listB.value?.random().toString()


        //---empfangene Daten---
        viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            //binding.tvData.text = "${data.playStatus}\n${data.seite}"
            val scope = MainScope()
            scope.launch {
                delay(10000)
                if (data.seite == prevSeite) {
                //    toast("Nochmal würfeln")
                } else {
                    binding.tvData.text = "Würfelseite: ${data.seite}"
                    if (data.seite == "1") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = true
                        binding.iv2.isVisible = false
                        binding.iv3.isVisible = false
                        binding.iv4.isVisible = false
                        binding.iv5.isVisible = false
                        binding.iv6.isVisible = false
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                    if (data.seite == "2") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = false
                        binding.iv2.isVisible = true
                        binding.iv3.isVisible = false
                        binding.iv4.isVisible = false
                        binding.iv5.isVisible = false
                        binding.iv6.isVisible = false
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                    if (data.seite == "3") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = false
                        binding.iv2.isVisible = false
                        binding.iv3.isVisible = true
                        binding.iv4.isVisible = false
                        binding.iv5.isVisible = false
                        binding.iv6.isVisible = false
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                    if (data.seite == "4") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = false
                        binding.iv2.isVisible = false
                        binding.iv3.isVisible = false
                        binding.iv4.isVisible = true
                        binding.iv5.isVisible = false
                        binding.iv6.isVisible = false
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                    if (data.seite == "5") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = false
                        binding.iv2.isVisible = false
                        binding.iv3.isVisible = false
                        binding.iv4.isVisible = false
                        binding.iv5.isVisible = true
                        binding.iv6.isVisible = false
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                    if (data.seite == "6") {
                        //viewModel.sendDataStop()
                        //viewModel.cancelDataLoadJob()
                        binding.iv1.isVisible = false
                        binding.iv2.isVisible = false
                        binding.iv3.isVisible = false
                        binding.iv4.isVisible = false
                        binding.iv5.isVisible = false
                        binding.iv6.isVisible = true
                        prevSeite = data.seite
                        Log.i(TAG, "Previous: $prevSeite")
                        showDialog()
                    }
                }
            }
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

    private fun getExercise() {
        db.collection("Sport").document("1")
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    return@EventListener
                }
                //updateListOnChange(value!!)
            })
    }

    private fun showDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialogT)
                .setMessage(prevSeite)
                .setPositiveButton(R.string.dialogYes) {dialog, which ->
                    viewModel.cancelDataLoadJob()
                    findNavController().navigate(R.id.action_gameFragment_to_exerciseFragment)
                }
                .setNegativeButton(R.string.dialogNo) {dialog, which ->
                    val scope = MainScope()
                    scope.launch {
                        delay(30000)
                        Log.i(TAG, "delay")
                        //viewModel.sendDataPlay()
                        viewModel.startDataLoadJob()
                    }
                    Log.i(TAG, "Nochmal würfeln bestätigt")
                }
                .show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //viewModel.sendDataStop()
        viewModel.cancelDataLoadJob()
    }
}