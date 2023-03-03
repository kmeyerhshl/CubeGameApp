package com.example.cubegameapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.SparseBooleanArray
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
import com.example.cubegameapp.databinding.FragmentPlayerBinding
import com.example.cubegameapp.model.ConnectState
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import splitties.toast.toast

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PlayerFragment : Fragment() {

    private val TAG = "PlayerFragment"

    private var _binding: FragmentPlayerBinding? = null

    private val viewModel: MainViewModel by activityViewModels()
    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var useSelected: String = ""

    private lateinit var playerList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>

    var bool1: Boolean = false
    var bool2: Boolean = false
    var bool3: Boolean = false

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


        //Verwendungszweck
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")

        //ListView ViewModel
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_multiple_choice,viewModel.getPlayerList()!!)
        binding.lvPlayer.adapter = adapter
        viewModel.playerList.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }


        //Spieler hinzufügen
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
                            viewModel.addPlayer(playerName)
                            adapter.notifyDataSetChanged()
                            Log.i(TAG,"neuer Spieler: $playerName")
                        }
                    }
                    .show()
            }
        }

        //Spieler löschen - kopiert
        binding.fabDelete.setOnClickListener {
            for (i in 0 until binding.lvPlayer.count) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.deletePlayer(playerName)
                }
            }
            adapter.notifyDataSetChanged()
        }



        binding.btnTeams.setOnClickListener {
            for (i in 0 until binding.lvPlayer.count) {
                if (!bool3) {
                    Log.i(TAG,"BOOL3 IS FALSE")
                    bool1 = false
                } else {
                    bool2 = true
                    Log.i(TAG,"BOOL3 IS TRUE")
                }
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.emptySelectedPlayers(playerName)
                    viewModel.selectPlayer(playerName)
                    if (!bool1) {
                        viewModel.selectPlayerA(playerName)
                        bool1 = true
                        Log.i(TAG,"BOOL1 IS FALSE")
                    }
                    if (bool2) {
                        viewModel.selectPlayerB(playerName)
                        bool2 = false
                        Log.i(TAG,"BOOL2 IS TRUE")
                    }
                    /*if (!bool1 && bool2) {
                        viewModel.selectPlayerA(playerName)
                        bool1 = true
                    }
                    if (bool1 && !bool2) {
                        viewModel.selectPlayerB(playerName)
                        bool1 = false
                    }*/
                    bool3 = !bool3
                }
            }
            adapter.notifyDataSetChanged()



            Log.i(TAG,"checked: ${binding.lvPlayer.checkedItemCount}")

            //bei 3 werden nur 2 zuzgeordnet
            /*for (i in 0 until binding.lvPlayer.checkedItemCount/2) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.selectPlayerA(playerName)
                }
            }
            for (i in binding.lvPlayer.checkedItemCount/2 until binding.lvPlayer.checkedItemCount) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.selectPlayerB(playerName)
                }
            }*/
            //Größe des gesamten LV wird geteilt
            /*for (i in 0 until binding.lvPlayer.count/2) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.selectPlayerA(playerName)
                }
            }
            for (i in binding.lvPlayer.count/2 until binding.lvPlayer.count) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    val playerName : String = binding.lvPlayer.getItemAtPosition(i) as String
                    viewModel.selectPlayerB(playerName)
                }
            }*/


            if (viewModel.selectedPlayerListA.value?.isEmpty() == true || viewModel.selectedPlayerListB.value?.isEmpty() == true) {
                Log.i(TAG, "Liste ist leer")
                toast("Bitte Spieler auswählen")
            } else {
                showDialogTeams()
            }
            /*var itemSelected = "Selected items: \n"
            for (i in 0 until binding.lvPlayer.count) {
                if (binding.lvPlayer.isItemChecked(i)) {
                    itemSelected += binding.lvPlayer.getItemAtPosition(i)
                    val playerName = binding.lvPlayer.getItemAtPosition(i)
                    viewModel.addPlayer(playerName as String)
                }
            }
            toast(itemSelected)*/
            /*val position: SparseBooleanArray = binding.lvPlayer.checkedItemPositions
            val count = binding.lvPlayer.count
            var item = count - 1
            while (item>=0) {
                if (position.get(item)) {

                }
            }*/
            //findNavController().navigate(R.id.action_SecondFragment_to_gameFragment)
        }

        // Mittels Observer über Änderungen des connect status informieren
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

        // Adapter is created and applied to ListView
        val mAdapterA = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, viewModel.getSelectedPlayerListA()!!)
        val mAdapterB = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, viewModel.getSelectedPlayerListB()!!)
        mListView.adapter = mAdapterA
        mListView2.adapter = mAdapterB
        mAdapterA.notifyDataSetChanged()
        mAdapterB.notifyDataSetChanged()


        // Row item is set as view in the Builder and the
        // ListView is displayed in the Alert Dialog
        //mAlertDialogBuilder.setTitle(getString(R.string.adTitle))
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