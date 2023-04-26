package com.example.dicegame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dicegame.databinding.FragmentPlayerBinding
import com.example.dicegame.model.ConnectState
import com.example.dicegame.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import splitties.toast.toast

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PlayerFragment : Fragment() {

    private val TAG = "PlayerFragment"

    //Variable für Verwendungszweck
    private var useSelected: String = ""

    //Boolean Variablen
    var bool1: Boolean = false
    var bool2: Boolean = false
    var bool3: Boolean = false

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentPlayerBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Verwendungszweck empfangen
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")

        //Spielernamen in Liste anzeigen
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_multiple_choice,viewModel.getPlayerList()!!)
        binding.lvPlayer.adapter = adapter
        viewModel.playerList.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

        //Liste der ausgewählten Spieler leeren (Spieler aus dem vorherigen Spiel)
        for (i in 0 until binding.lvPlayer.count) {
            val player: String = binding.lvPlayer.getItemAtPosition(i) as String
            viewModel.emptySelectedPlayers(player)
        }

        //Spieler hinzufügen: Namen eintippen und bestätigen
        binding.fabAdd.setOnClickListener {
            val editTextView = EditText(this.context)
            this?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialogAdd)
                    .setView(editTextView)
                    .setNeutralButton(R.string.dialog_cancel2) { dialog, which ->
                    }
                    .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                        val playerName = editTextView.text.toString()
                        if (playerName.isEmpty()) {
                            toast(getString(R.string.fill_out))
                        } else {
                            //Spieler wird der Spielerliste hinzugefügt und in der Liste angezeigt
                            viewModel.addPlayer(playerName)
                            adapter.notifyDataSetChanged()
                            Log.i(TAG,"neuer Spieler: $playerName")
                        }
                    }
                    .show()
            }
        }


        //Spieler löschen
        binding.fabDelete.setOnClickListener {
            //Spielerliste durchgehen und prüfen, welcher Spieler ausgewählt wurde
            for (i in 0 until binding.lvPlayer.count) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    //ausgewählte Spieler aus der Spielerliste löschen
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.deletePlayer(playerName)
                }
            }
            //ListView aktualisieren
            adapter.notifyDataSetChanged()
        }


        //ausgewählte Spieler abwechselnd zu Team A und Team B zuordnen
        binding.btnTeams.setOnClickListener {
            //Liste der Spieler durchgehen
            for (i in 0 until binding.lvPlayer.count) {
                //wenn bool3 false ist, wird bool1 false --> Spieler geht zu Team A
                if (!bool3) {
                    Log.i(TAG,"BOOL3 IS FALSE")
                    bool1 = false
                } else {
                    //wenn bool3 true ist, wird bool2 true --> Spieler geht zu Team B
                    bool2 = true
                    Log.i(TAG,"BOOL3 IS TRUE")
                }
                //wenn ein Spieler ausgewählt wurde
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    //Spielernamen aus allen Listen ausgewählter Spieler löschen
                    viewModel.emptySelectedPlayers(playerName)
                    //Spielernamen zur Liste ausgewählter Spieler hinzufügen
                    viewModel.selectPlayer(playerName)
                    if (!bool1) {
                        //wenn bool1 false ist, Spielernamen zu Team A hinzufügen
                        viewModel.selectPlayerA(playerName)
                        //bool1 true setzen
                        bool1 = true
                        Log.i(TAG,"BOOL1 IS FALSE")
                    }
                    if (bool2) {
                        //wenn bool2 false ist, Spielernamen zu Team B hinzufügen
                        viewModel.selectPlayerB(playerName)
                        //bool2 false setzen
                        bool2 = false
                        Log.i(TAG,"BOOL2 IS TRUE")
                    }
                    //bool3 toggeln --> gleichmäßige Teamaufteilung ermöglichen
                    bool3 = !bool3
                }
            }
            adapter.notifyDataSetChanged()

            Log.i(TAG,"checked: ${binding.lvPlayer.checkedItemCount}")

            //wenn keine Spieler ausgewählt wurden (beide Teamlisten leer sind), erscheint ein Hinweis
            if (viewModel.selectedPlayerListA.value?.isEmpty() == true || viewModel.selectedPlayerListB.value?.isEmpty() == true) {
                Log.i(TAG, "Liste ist leer")
                toast("Bitte Spieler auswählen")
            } else {
                //sonst wird das Alert Dialog mit der Teamaufteilung angezeigt
                showDialogTeams()
            }
        }

        // Mittels Observer über Änderungen des connect status informieren
        // wenn die Bluetooth-Verbindung abbricht oder kein Gerät verbunden ist, wird die Startseite erneut geöffnet
        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConnectState.NOT_CONNECTED -> {
                    toast("Bluetooth-Verbindung abgebrochen")
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
                ConnectState.NO_DEVICE -> {
                    toast("kein Bluetooth Gerät")
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
        }
    }

    private fun showDialogTeams() {
        //Alert Dialog
        val mAlertDialogBuilder = AlertDialog.Builder(requireContext())
        // Row layout is inflated and added to ListView
        val mRowList = layoutInflater.inflate(R.layout.listview, null)
        val mListView = mRowList.findViewById<ListView>(R.id.list_view_1)
        val mListView2 = mRowList.findViewById<ListView>(R.id.list_view_2)

        //Adapter für Team A und Adapter für Team B
        val mAdapterA = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, viewModel.getSelectedPlayerListA()!!)
        val mAdapterB = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, viewModel.getSelectedPlayerListB()!!)
        mListView.adapter = mAdapterA
        mListView2.adapter = mAdapterB
        mAdapterA.notifyDataSetChanged()
        mAdapterB.notifyDataSetChanged()

        //ListViews für die beiden Teams werden untereinander im Alert Dialog angezeigt
        mAlertDialogBuilder.setView(mRowList)
        mAlertDialogBuilder.setPositiveButton(getString(R.string.posButton)) { dialog, which ->
            findNavController().navigate(R.id.action_SecondFragment_to_gameFragment)
        }
        val dialog = mAlertDialogBuilder.create()
        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}