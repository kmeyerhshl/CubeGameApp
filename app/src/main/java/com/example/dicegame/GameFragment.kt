package com.example.dicegame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dicegame.databinding.FragmentGameBinding
import com.example.dicegame.model.ConnectState
import com.example.dicegame.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    //Verwendungszweck
    private var useSelected: String = ""

    //Variablen zur Rundenauswahl
    val singleItems = arrayOf("3","5","10")
    var checkedItem = 1
    var nrRounds : Int = 0
    var roundsVM: Int = 0
    var counterVM: Int = 0

    //Variablen für Zeitberechnung
    var timeDif1: Long = 0
    var timeDif2: Long = 0
    var timeDif3: Long = 0
    var timeDif4: Long = 0
    var timeDif5: Long = 0
    var timeDif6: Long = 0

    //Variablen und Boolean für Zeitmessung
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

    //Variable vorherige Seite
    private var prevSeite: String = ""


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
        //Wert für bool2 überprüfen
        viewModel.booleanNext.observe(viewLifecycleOwner) {bool2 ->
            //Rundenanzahl wurde noch nicht ausgewählt --> bool2 ist false
            if (!bool2) {
                //bool2 wird true gesetzt --> Rundenanzahl wurde ausgewählt
                viewModel.switchBoolean()
                //Alert Dialog zur Auswahl der Rundenanzahl: 3, 5 oder 11 Runden
                var selectedItem = singleItems[checkedItem]
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(resources.getString(R.string.dialogTitle))
                    .setPositiveButton(resources.getString(R.string.dialogEnter)) { dialog, which ->
                        //gewählte Rundenanzahl wird an ESP32 gesendet
                        viewModel.sendRoundData(selectedItem.toInt())
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
                        if (selectedItem == "11") {
                            nrRounds = 11
                            viewModel.setRoundsSelected(nrRounds)
                            Log.i(TAG,"ausgewählte Rundenanzahl: $nrRounds")
                        }
                        nrRounds = selectedItem.toInt()
                        Log.i(TAG,"Rundennummer: $nrRounds")
                        //Befehl zum Starten des Spiels wird an ESP32 gesendet
                        viewModel.gameStatus.gameStatus = "P"
                        viewModel.sendGameStatus()
                        //Datenempfang wird gestartet
                        viewModel.startDataLoadJob()

                        //aktuelle Runde
                        viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                            counterVM = counter
                            Log.i(TAG,"Counter: $counterVM")
                        }
                        //ausgewählte Rundenanzahl
                        viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                            roundsVM = roundsSelected
                            Log.i(TAG,"Rounds: $roundsVM")
                        }
                        //Anzeige der aktuellen Runde und der ausgewählten Rundenanzahl
                        binding.tvRunden.text = "Runde: $counterVM / $roundsVM"
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                        checkedItem = which
                        selectedItem = singleItems[which]
                    }
                    .show()
            } else {
                //Rundenanzahl wurde bereits ausgewählt --> bool2 ist true
                Log.i(TAG, "Rundenanzahl bereits ausgewählt")
                //aktuelle Runde
                viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                    counterVM = counter
                    Log.i(TAG,"Counter: $counterVM")
                }
                //ausgewählte Rundenanzahl
                viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                    roundsVM = roundsSelected
                    Log.i(TAG,"Rounds: $roundsVM")
                }
                //Anzeige der aktuellen Runde und der ausgewählten Rundenanzahl
                binding.tvRunden.text = "Runde: $counterVM / $roundsVM"
                //Befehl zum Starten des Spiels wird an ESP32 gesendet
                viewModel.gameStatus.gameStatus = "P"
                viewModel.sendGameStatus()
                //Datenempfang wird gestartet
                viewModel.startDataLoadJob()
            }
        }

        //---Spielstand Team A anzeigen---
        viewModel.counterA.observe(viewLifecycleOwner) {counterA ->
            binding.tvScoreA.text = counterA.toString()
        }

        //---Spielstand Team B anzeigen---
        viewModel.counterB.observe(viewLifecycleOwner) {counterB ->
            binding.tvScoreB.text = counterB.toString()
        }




        //---empfangene Daten überprüfen---
        viewModel.esp32Data.observe(viewLifecycleOwner) { data ->
            //Wert für bool1 überprüfen
            viewModel.booleanAD.observe(viewLifecycleOwner) { bool1 ->
                //so lange bool1 false ist
                if (!bool1) {
                    //wenn empfangene Seite der vorherigen entspricht, soll nochmal gewürfelt werden
                    if (data.seite == prevSeite) {
                        Log.i(TAG, "Nochmal würfeln")
                    } else {
                        //sonst wird überprüft, welchem Wert die empfangene Seite entspricht
                        binding.tvData.text = "Würfelseite: ${data.seite}"
                        //---Seite 1---
                        if (data.seite == "1") {
                            //wenn Seite 1 empfangen wurde, wird das Bild mit der 1 angezeigt
                            binding.iv1.isVisible = true
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            //wenn bool3 false ist, startet die Zeitmessung
                            //bool3 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif1 = Duration.between(time1, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer1: $timeDif1")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif1 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                        //---Seite 2---
                        if (data.seite == "2") {
                            //wenn Seite 2 empfangen wurde, wird das Bild mit der 2 angezeigt
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = true
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            //wenn bool4 false ist, startet die Zeitmessung
                            //bool4 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif2 = Duration.between(time2, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer2: $timeDif2")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif2 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                        //---Seite 3---
                        if (data.seite == "3") {
                            //wenn Seite 3 empfangen wurde, wird das Bild mit der 3 angezeigt
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = true
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            //wenn bool5 false ist, startet die Zeitmessung
                            //bool5 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif3 = Duration.between(time3, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer3: $timeDif3")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif3 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                        //---Seite 4---
                        if (data.seite == "4") {
                            //wenn Seite 4 empfangen wurde, wird das Bild mit der 4 angezeigt
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = true
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = false
                            //wenn bool6 false ist, startet die Zeitmessung
                            //bool6 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif4 = Duration.between(time4, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer4: $timeDif4")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif4 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                        //---Seite 5---
                        if (data.seite == "5") {
                            //wenn Seite 5 empfangen wurde, wird das Bild mit der 5 angezeigt
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = true
                            binding.iv6.isVisible = false
                            //wenn bool7 false ist, startet die Zeitmessung
                            //bool7 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif5 = Duration.between(time5, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer5: $timeDif5")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif5 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                        //---Seite 6---
                        if (data.seite == "6") {
                            //wenn Seite 6 empfangen wurde, wird das Bild mit der 6 angezeigt
                            binding.iv1.isVisible = false
                            binding.iv2.isVisible = false
                            binding.iv3.isVisible = false
                            binding.iv4.isVisible = false
                            binding.iv5.isVisible = false
                            binding.iv6.isVisible = true
                            //wenn bool8 false ist, startet die Zeitmessung
                            //bool8 wird true, damit die Schleife verlassen wird
                            //die boolean-Werte der anderen Schleifen werden auf false gesetzt
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
                            //Differenz zwischen der aktuellen und der gestarteten Zeit berechnen
                            timeDif6 = Duration.between(time6, LocalDateTime.now()).seconds
                            Log.i(TAG, "Dauer6: $timeDif6")
                            //wenn die Differenz mehr als 10 beträgt --> gleiche Seite wird für längere Zeit empfangen
                            if (timeDif6 > 10) {
                                //aktuelle Seite wird als vorherige Seite gespeichert
                                prevSeite = data.seite
                                Log.i(TAG, "Previous: $prevSeite")
                                viewModel.setDicedSide(prevSeite)
                                //Alert Dialog wird geöffnet
                                showDialog()
                            }
                        }
                    }
                }
            }
        }

        // Mittels Observer über Änderungen des connect status informieren
        // wenn die Bluetooth-Verbindung abbricht oder kein Gerät verbunden ist, wird die Startseite erneut geöffnet
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


    //AlertDialog zur Auswahl zwischen nochmal würfeln und Runde starten
    private fun showDialog() {
        //booleanAD toggeln --> empfangene Daten werden nicht mehr überprüft
        viewModel.switchAD()
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialogT)
                .setMessage(prevSeite)
                .setPositiveButton(R.string.dialogYes) {dialog, which ->
                    //Stoppsignal an ESP32 senden
                    viewModel.gameStatus.gameStatus = "S"
                    viewModel.sendGameStatus()
                    //Datenempfang stoppen
                    viewModel.cancelDataLoadJob()
                    //ExerciseFragment öffnen
                    findNavController().navigate(R.id.action_gameFragment_to_exerciseFragment)
                }
                .setNegativeButton(R.string.dialogNo) {dialog, which ->
                    //delay abwarten
                    val scope = MainScope()
                    scope.launch {
                        delay(3000)
                        Log.i(TAG, "delay")
                        //Variablen zurücksetzen
                        resetVar()
                        //booleanAD toggeln --> empfangene Daten werden wieder überprüft
                        viewModel.switchAD()
                        //Datenempfang wird gestartet
                        viewModel.startDataLoadJob()
                    }
                    Log.i(TAG, "Nochmal würfeln bestätigt")
                }
                .show()
        }
    }

    //Variablen zurücksetzen
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
        //Datenempfang stoppen
        viewModel.cancelDataLoadJob()
    }
}