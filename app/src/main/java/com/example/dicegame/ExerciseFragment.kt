package com.example.dicegame

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dicegame.databinding.FragmentExerciseBinding
import com.example.dicegame.model.ConnectState
import com.example.dicegame.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import splitties.toast.toast
import java.io.File


class ExerciseFragment : Fragment() {

    private val TAG = "ExerciseFragment"

    private var _binding: FragmentExerciseBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    //Variablen Spielstand
    private var winner: String = ""
    private var scoreA: Int = 0
    private var scoreB: Int = 0
    private var scoreA1: Int = 0
    private var scoreB1: Int = 0
    private var gameOver = false
    var roundsVM: Int = 0
    var counterVM: Int = 0

    //Variablen für Übung
    private var useSelected: String = ""
    private var dicedSide: String = ""


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


        //abhängig vom ausgewählten Verwendungszweck und der gewürfelten Seite die zugehörige Übung aus dem Storage laden
        //Übung/Bild in Datei ablegen und im Layout darstellen
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
        //zufälligen Namen aus der Spielerliste des Teams A anzeigen
        val listA = viewModel.selectedPlayerListA
        binding.tvNameA.text = listA.value?.random().toString()


        //---Name Team B---
        //zufälligen Namen aus der Spielerliste des Teams B anzeigen
        val listB = viewModel.selectedPlayerListB
        binding.tvNameB.text = listB.value?.random().toString()



        //---Gewinner wählen---
        binding.btnA.setOnClickListener {
            //Punktestand von Team A erhöhen
            viewModel.incCounterA()
            //Rundenanzahl erhöhen
            viewModel.incBtnCounter()
            Log.i(TAG, "Runde: $viewModel.btnCounter")
            //counterA beobachten
            viewModel.counterA.observe(viewLifecycleOwner) {counterA ->
                scoreA1 = counterA
            }
            //aktuelle Rundennummer beobachten
            viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                counterVM = counter
                Log.i(TAG,"Counter: $counterVM")
            }
            //ausgewählte Rundenanzahl beobachten
            viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                roundsVM = roundsSelected
                Log.i(TAG,"Rounds: $roundsVM")
            }
            var counter = counterVM - 1
            Log.i(TAG, "COUNTER Abgezogen: $counter")
            //wenn counter dem Wert der ausgewählten Rundenanzahl entspricht --> Spiel ist vorbei
            if (counter == roundsVM) {
                Log.i(TAG, "Status: $gameOver")
                //Spielstand prüfen
                checkRound()
            } else {
                //Boolean für Datenübertragung toggeln --> bool1 in GameFragment ist wieder false
                viewModel.switchAD()
                //Spielstand als Toast ausgeben
                toast(getString(R.string.scoreA).format(scoreA1))
                //GameFragment öffnen
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }
        }

        //---Gewinner wählen---
        binding.btnB.setOnClickListener {
            //Punktestand von Team B erhöhen
            viewModel.incCounterB()
            //Rundenanzahl erhöhen
            viewModel.incBtnCounter()
            Log.i(TAG, "Runde: $viewModel.btnCounter")
            //counterB beobachten
            viewModel.counterB.observe(viewLifecycleOwner) {counterB ->
                scoreB1 = counterB
            }
            //aktuelle Rundennummer beobachten
            viewModel.btnCounter.observe(viewLifecycleOwner) {counter ->
                counterVM = counter
                Log.i(TAG,"Counter: $counterVM")
            }
            //ausgewählte Rundenanzahl beobachten
            viewModel.roundsSelected.observe(viewLifecycleOwner) {roundsSelected ->
                roundsVM = roundsSelected
                Log.i(TAG,"Rounds: $roundsVM")
            }
            var counter = counterVM - 1
            Log.i(TAG, "COUNTER Abgezogen: $counter")
            //wenn counter dem Wert der ausgewählten Rundenanzahl entspricht --> Spiel ist vorbei
            if (counter == roundsVM) {
                Log.i(TAG, "Status: $gameOver")
                //Spielstand prüfen
                checkRound()
            } else {
                //Boolean für Datenübertragung toggeln --> bool1 in GameFragment ist wieder false
                viewModel.switchAD()
                //Spielstand als Toast ausgeben
                toast(getString(R.string.scoreB).format(scoreB1))
                //GameFragment öffnen
                findNavController().navigate(R.id.action_exerciseFragment_to_gameFragment)
            }
        }

        // Mittels Observer über Änderungen des connect status informieren
        // wenn die Bluetooth-Verbindung abbricht oder kein Gerät verbunden ist, wird die Startseite erneut geöffnet
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

    //Spielstand am Spielende überprüfen
    private fun checkRound() {
        Log.i(TAG, "Spiel vorbei")
        //counterA beobachten
        viewModel.counterA.observe(viewLifecycleOwner){
                counterA -> scoreA = counterA
        }
        //counterB beobachten
        viewModel.counterB.observe(viewLifecycleOwner){
                counterB -> scoreB = counterB
        }
        //wenn Team A mehr Punkte hat als Team B --> Team A hat gewonnen
        if(scoreA > scoreB) {
            winner = "Team A"
        } else {
            //wenn Team B mehr Punkte hat als Team A --> Team B hat gewonnen
            winner = "Team B"
        }
        //Alert Dialog mit Anzeige des Gewinners
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialogEnd))
            .setMessage(winner)
            .setPositiveButton(resources.getString(R.string.dialogEnter)) { dialog, which ->
                //"Spiel beendet" an ESP32 senden
                viewModel.gameStatus.gameStatus = "F"
                viewModel.sendGameStatus()
                //Datenempfang beenden
                viewModel.cancelDataLoadJob()
                //Boolean für Auswahl der Rundenanzahl "zurücksetzen" --> bool2 wird false
                viewModel.switchBoolean()
                //Boolean für Datenüberprüfung "zurücksetzen" --> bool1 wird false
                viewModel.switchAD()
                //Spielstand von Team A und Team B und Wert der aktuellen Runde zurücksetzen
                viewModel.resetCounterA()
                viewModel.resetCounterB()
                viewModel.resetBtnCounter()
                //HomeFragment öffnen
                findNavController().navigate(R.id.action_exerciseFragment_to_FirstFragment)
            }
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}