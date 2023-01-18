package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cubegameapp.databinding.FragmentPlayerBinding
import com.example.cubegameapp.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

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

        //var itemlist = arrayListOf<String>()
        //var adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, itemlist)


        //Verwendungszweck
        useSelected = viewModel.getUseSelected()
        Log.i(TAG, "Verwendungszweck: $useSelected")

        //ListView ViewModel
        //val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_multiple_choice,viewModel.getPlayerList()!!)
        //binding.lvPlayer.adapter = adapter
        //viewModel.playerList.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }


        //ListView ohne ViewModel
        playerList = ArrayList()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, playerList)
        binding.lvPlayer.adapter = adapter


        //Spieler löschen
        binding.fabDelete.setOnClickListener {
            val position: SparseBooleanArray = binding.lvPlayer.checkedItemPositions
            val count = binding.lvPlayer.count
            var item = count - 1
            while (item>=0) {
                if (position.get(item)) {
                    adapter.remove(playerList.get(item))
                }
                item--
            }
            position.clear()
            adapter.notifyDataSetChanged()

            /*val position: SparseBooleanArray = binding.lvPlayer.checkedItemPositions
            val count = binding.lvPlayer.count
            var item = count - 1
            for (i in 0 until count) {
                viewModel.deletePlayer(binding.lvPlayer.getItemAtPosition(i).toString())
            }
            while (item >= 0) {
                if (position.get(item)) {
                    //adapter.remove(itemlist.get(item))
                    viewModel.deletePlayer()
                    //adapter.remove(binding.lvPlayer.get(item).toString())
                }
                item--
            }
            position.clear()
            adapter.notifyDataSetChanged()*/
        }


        /*binding.lvPlayer.setOnItemClickListener { adapterView, view, i, l ->
            showDialogDelete(binding.lvPlayer.getItemAtPosition(i).toString())
        }*/


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
                        playerList.add(playerName)
                        adapter.notifyDataSetChanged()
                        //viewModel.addPlayer(playerName)
                        Log.i(TAG,"neuer Spieler: $playerName")
                    }
                    .show()
            }
        }

        binding.btnTeams.setOnClickListener {

        }

        //Button zurück
        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    private fun showDialogAdd() {
        val editTextView = EditText(this.context)
        this?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialogAdd)
                .setView(editTextView)
                .setNeutralButton(R.string.dialog_cancel2) { dialog, which ->
                }
                .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                    val playerName = editTextView.text.toString()
                    playerList.add(playerName)
                    adapter.notifyDataSetChanged()
                    //viewModel.addPlayer(playerName)
                    Log.i(TAG,"neuer Spieler: $playerName")
                }
                .show()
        }
    }

    /*private fun showDialogDelete(selectedItem: String) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialogDelete)
                .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                    viewModel.deletePlayer(selectedItem)
                }
                .show()
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}