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
import java.time.Duration
import java.time.LocalDateTime


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

    var timeDif1: Long = 0
    var timeDif2: Long = 0
    var timeDif3: Long = 0
    var timeDif4: Long = 0
    var timeDif5: Long = 0
    var timeDif6: Long = 0


    var bool3: Boolean = false
    private lateinit var time1 : LocalDateTime
    var bool4: Boolean = false
    private lateinit var time2 : LocalDateTime
    var bool5: Boolean = false
    private lateinit var time3 : LocalDateTime
    var bool6: Boolean = false
    private lateinit var time4 : LocalDateTime
    var bool7: Boolean = false
    private lateinit var time5 : LocalDateTime
    var bool8: Boolean = false
    private lateinit var time6 : LocalDateTime


    private var newA: String = ""
    private var newB: String = ""
    private var prevSeite: String = ""




    // This property is only valid between onCreateView and onDestroyView.
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
                //viewModel.sendRoundData(viewModel.getRoundsSelected())
                viewModel.gameStatus.gameStatus = "P"
                viewModel.sendGameStatus()
                viewModel.startDataLoadJob()
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
        //funktioniert, aber setzt nicht auf 0 zurück
        /*viewModel.esp32Data.observe(viewLifecycleOwner) { data ->
            viewModel.booleanAD.observe(viewLifecycleOwner) { bool1 ->
                if (!bool1) {
                    if (data.seite == prevSeite) {
                        Log.i(TAG, "Nochmal würfeln")
                    } else {
                        binding.tvData.text = "Würfelseite: ${data.seite}"
                        if (data.seite == "1") {
                            if (!bool3) {
                                time1 = LocalDateTime.now()
                                bool3 = true
                            }
                            val timeDif = Duration.between(time1, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer1: $timeDif")
                            if (timeDif > 10) {
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
                        }
                        if (data.seite == "2") {
                            if (!bool4) {
                                time2 = LocalDateTime.now()
                                bool4 = true
                            }
                            val timeDif = Duration.between(time2, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer2: $timeDif")
                            if (timeDif > 10) {
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
                        }
                        if (data.seite == "3") {
                            if (!bool5) {
                                time3 = LocalDateTime.now()
                                bool5 = true
                            }
                            val timeDif = Duration.between(time3, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer3: $timeDif")
                            if (timeDif > 10) {
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
                        }
                        if (data.seite == "4") {
                            if (!bool6) {
                                time4 = LocalDateTime.now()
                                bool6 = true
                            }
                            val timeDif = Duration.between(time4, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer4: $timeDif")
                            if (timeDif > 10) {
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
                        }
                        if (data.seite == "5") {
                            if (!bool7) {
                                time5 = LocalDateTime.now()
                                bool7 = true
                            }
                            val timeDif = Duration.between(time5, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer5: $timeDif")
                            if (timeDif > 10) {
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
                        }
                        if (data.seite == "6") {
                            if (!bool8) {
                                time6 = LocalDateTime.now()
                                bool8 = true
                            }
                            val timeDif = Duration.between(time6, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer6: $timeDif")
                            if (timeDif > 10) {
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
            }
        }*/
        viewModel.esp32Data.observe(viewLifecycleOwner) { data ->
            viewModel.booleanAD.observe(viewLifecycleOwner) { bool1 ->
                if (!bool1) {
                    if (data.seite == prevSeite) {
                        Log.i(TAG, "Nochmal würfeln")
                    } else {
                        binding.tvData.text = "Würfelseite: ${data.seite}"
                        //---Seite 1---
                        if (data.seite == "1") {
                            binding.iv1.isVisible = true
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            if (!bool3) {
                                time1 = LocalDateTime.now()
                                bool3 = true
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = false
                                timeDif1 = 0
                            }
                            timeDif1 = Duration.between(time1, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer1: $timeDif1")
                            if (timeDif1 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                        //---Seite 2---
                        if (data.seite == "2") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = true
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            if (!bool4) {
                                time2 = LocalDateTime.now()
                                bool3 = false
                                bool4 = true
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = false
                                timeDif2 = 0
                            }
                            timeDif2 = Duration.between(time2, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer2: $timeDif2")
                            if (timeDif2 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                        //---Seite 3---
                        if (data.seite == "3") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = true
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            if (!bool5) {
                                time3 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = true
                                bool6 = false
                                bool7 = false
                                bool8 = false
                                timeDif3 = 0
                            }
                            timeDif3 = Duration.between(time3, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer3: $timeDif3")
                            if (timeDif3 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                        //---Seite 4---
                        if (data.seite == "4") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = true
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            if (!bool6) {
                                time4 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = true
                                bool7 = false
                                bool8 = false
                                timeDif4 = 0
                            }
                            timeDif4 = Duration.between(time4, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer4: $timeDif4")
                            if (timeDif4 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                        //---Seite 5---
                        if (data.seite == "5") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = true
                            binding.iv6.isVisible = false
                            if (!bool7) {
                                time5 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = true
                                bool8 = false
                                timeDif5 = 0
                            }
                            timeDif5 = Duration.between(time5, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer5: $timeDif5")
                            if (timeDif5 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                        //---Seite 6---
                        if (data.seite == "6") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = true
                            if (!bool8) {
                                time6 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = true
                                timeDif6 = 0
                            }
                            timeDif6 = Duration.between(time6, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer6: $timeDif6")
                            if (timeDif6 > 10) {
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                showDialog()
                            }
                        }
                    }
                }
            }
        }
        //zeitmessung
        /*viewModel.esp32Data.observe(viewLifecycleOwner) { data ->
            viewModel.booleanAD.observe(viewLifecycleOwner) { bool1 ->
                if (!bool1) {
                    if (data.seite == prevSeite) {
                        Log.i(TAG, "Nochmal würfeln")
                    } else {
                        binding.tvData.text = "Würfelseite: ${data.seite}"
                        //---Seite 1---
                        if (data.seite == "1") {
                            if (!bool3) {
                                time1 = LocalDateTime.now()
                                bool3 = true
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = false
                            }
                            val timeDif = Duration.between(time1, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer1: $timeDif")

                            binding.iv1.isVisible = true
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                        //---Seite 2---
                        if (data.seite == "2") {
                            if (!bool4) {
                                time2 = LocalDateTime.now()
                                bool3 = false
                                bool4 = true
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = false
                            }
                            val timeDif = Duration.between(time2, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer2: $timeDif")

                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = true
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                        //---Seite 3---
                        if (data.seite == "3") {
                            if (!bool5) {
                                time3 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = true
                                bool6 = false
                                bool7 = false
                                bool8 = false
                            }
                            val timeDif = Duration.between(time3, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer3: $timeDif")

                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = true
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                        //---Seite 4---
                        if (data.seite == "4") {
                            if (!bool6) {
                                time4 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = true
                                bool7 = false
                                bool8 = false
                            }
                            val timeDif = Duration.between(time4, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer4: $timeDif")

                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = true
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                        //---Seite 5---
                        if (data.seite == "5") {
                            if (!bool7) {
                                time5 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = true
                                bool8 = false
                            }
                            val timeDif = Duration.between(time5, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer5: $timeDif")

                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = true
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                        //---Seite 6---
                        if (data.seite == "6") {
                            if (!bool8) {
                                time6 = LocalDateTime.now()
                                bool3 = false
                                bool4 = false
                                bool5 = false
                                bool6 = false
                                bool7 = false
                                bool8 = true
                            }
                            val timeDif = Duration.between(time6, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer6: $timeDif")

                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = true
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")

                            if (timeDif > 5) {
                                showDialog()
                            }
                        }
                    }
                }
            }
        }*/
        //Absturz?
        /*viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            val scope = MainScope()
            scope.launch {
                delay(5000)
                viewModel.booleanAD.observe(viewLifecycleOwner) {bool1 ->
                    if (!bool1) {
                        if (data.seite == prevSeite) {
                            Log.i(TAG, "Nochmal würfeln")
                        } else {
                            binding.tvData.text = "Würfelseite: ${data.seite}"
                            if (data.seite == "1") {
                                if (!bool3) {
                                    time1 = LocalDateTime.now()
                                    bool3 = true
                                }
                                val timeDif = Duration.between(time1, LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer1: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                            if (data.seite == "2") {
                                if (!bool4) {
                                    time2 = LocalDateTime.now()
                                    bool4 = true
                                }
                                val timeDif = Duration.between(time2,LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer2: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                            if (data.seite == "3") {
                                if (!bool5) {
                                    time3 = LocalDateTime.now()
                                    bool5 = true
                                }
                                val timeDif = Duration.between(time3,LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer3: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                            if (data.seite == "4") {
                                if (!bool6) {
                                    time4 = LocalDateTime.now()
                                    bool6 = true
                                }
                                val timeDif = Duration.between(time4,LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer4: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                            if (data.seite == "5") {
                                if (!bool7) {
                                    time5 = LocalDateTime.now()
                                    bool7 = true
                                }
                                val timeDif = Duration.between(time5,LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer5: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                            if (data.seite == "6") {
                                if (!bool8) {
                                    time6 = LocalDateTime.now()
                                    bool8 = true
                                }
                                val timeDif = Duration.between(time6,LocalDateTime.now()).seconds
                                Log.i(TAG, "Dauer6: $timeDif")
                                if (timeDif > 5) {
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
                                    //viewModel.switchAD()
                                    showDialog()
                                }
                            }
                        }
                    }
                }
            }
        }*/
        //mit delay
        /*viewModel.esp32Data.observe(viewLifecycleOwner) {data ->
            val scope = MainScope()
            scope.launch {
                delay(5000)
                if (data.seite == prevSeite) {
                    Log.i(TAG, "Nochmal würfeln")
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
                        viewModel.switchAD()
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
                        viewModel.switchAD()
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
                        viewModel.switchAD()
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
                        viewModel.switchAD()
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
                        viewModel.switchAD()
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
                        viewModel.switchAD()
                    }
                }
            }
        }*/
        //nur Würfelbild ändern ohne weiteres
        /*viewModel.esp32Data.observe(viewLifecycleOwner) { data ->
            viewModel.booleanAD.observe(viewLifecycleOwner) { bool1 ->
                if (!bool1) {
                    if (data.seite == prevSeite) {
                        Log.i(TAG, "Nochmal würfeln")
                    } else {
                        binding.tvData.text = "Würfelseite: ${data.seite}"
                        if (data.seite == "1") {
                            binding.iv1.isVisible = true
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                        if (data.seite == "2") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = true
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                        if (data.seite == "3") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = true
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                        if (data.seite == "4") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = true
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                        if (data.seite == "5") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = true
                            binding.iv6.isVisible = false
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                        if (data.seite == "6") {
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = true
                            prevSeite = data.seite
                            Log.i(TAG, "Previous: $prevSeite")
                            //showDialog()
                        }
                    }
                }
            }
        }*/

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



    private fun showDialog() {
        viewModel.switchAD()
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialogT)
                .setMessage(prevSeite)
                .setPositiveButton(R.string.dialogYes) {dialog, which ->
                    viewModel.gameStatus.gameStatus = "S"
                    viewModel.sendGameStatus()
                    viewModel.cancelDataLoadJob()
                    findNavController().navigate(R.id.action_gameFragment_to_exerciseFragment)
                }
                .setNegativeButton(R.string.dialogNo) {dialog, which ->
                    val scope = MainScope()
                    scope.launch {
                        delay(3000)
                        Log.i(TAG, "delay")
                        resetVar()
                        viewModel.switchAD()
                        viewModel.startDataLoadJob()
                    }
                    Log.i(TAG, "Nochmal würfeln bestätigt")
                }
                .show()
        }
    }

    private fun resetVar() {
        bool3 = false
        bool4 = false
        bool5 = false
        bool6 = false
        bool7 = false
        bool8 = false

        timeDif1 = 0
        timeDif2 = 0
        timeDif3 = 0
        timeDif4 = 0
        timeDif5 = 0
        timeDif6 = 0
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //viewModel.sendDataStop()
        viewModel.cancelDataLoadJob()
    }
}