package com.example.cubegameapp

import android.graphics.BitmapFactory
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import splitties.toast.toast
import java.io.File


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

    private var useSelected: String = ""
    private var dicedSide: String = ""

    private var newA: String = ""
    private var newB: String = ""

    //Variablen für Datenbank
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    //private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // This property is only valid between onCreateView and onDestroyView.
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

        //Verwendungszweck
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")
        //Würfelseite
        dicedSide = viewModel.getDicedSide()
        Log.i(TAG,"Würfelseite: $dicedSide")


        val storageRef = FirebaseStorage.getInstance().reference.child("$useSelected/$dicedSide.jpg")
        val localFile = File.createTempFile("images","jpg")
        storageRef.getFile(localFile)
            .addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                binding.ivExercise.setImageBitmap(bitmap)
            }
            .addOnFailureListener {
                toast("Fehler")
            }


        //---Name Team A---
        val listA = viewModel.selectedPlayerListA
        binding.tvNameA.text = listA.value?.random().toString()
        //newA = listA.value?.random().toString()
        //binding.tvNameA.text = getString(R.string.tvNameA).format(newA)

        //---Name Team B---
        val listB = viewModel.selectedPlayerListB
        binding.tvNameB.text = listB.value?.random().toString()
        //newB = listB.value?.random().toString()
        //binding.tvNameB.text = getString(R.string.tvNameB).format(newB)


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
                viewModel.switchAD()
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }
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
                viewModel.switchAD()
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }
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
                //viewModel.sendDataStop()
                viewModel.gameStatus.gameStatus = "F"
                viewModel.sendGameStatus()
                viewModel.cancelDataLoadJob()
                viewModel.switchBoolean()
                viewModel.switchAD()
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
    }
}