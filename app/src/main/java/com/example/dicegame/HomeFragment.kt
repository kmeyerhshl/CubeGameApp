package com.example.dicegame

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dicegame.databinding.FragmentHomeBinding
import com.example.dicegame.model.ConnectState
import com.example.dicegame.model.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import splitties.toast.toast

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private val TAG = "Home"

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var dbList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>

    private var use: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startScan()
        val scope = MainScope()
        scope.launch {
            delay(1000)
            viewModel.connect()
        }

        binding.textviewFirst.text = viewModel.getDeviceSelected()

        loadDbList()

        binding.autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            toast("${binding.autoCompleteTextView.text}")
            use = binding.autoCompleteTextView.text.toString()
            viewModel.setUseSelected(use)
        }

        binding.btnStart.setOnClickListener {
            Log.i(TAG, "Button Start")
            if (use.isEmpty()) {
                toast("Bitte Zweck auswählen")
            } else {
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }
        }


        binding.fabAnleitung.setOnClickListener {
            showDialog()
        }


        // Mittels Observer über Änderungen des connect status informieren
        viewModel.connectState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConnectState.CONNECTED -> {
                    binding.tvConnection.text = getString(R.string.connected)
                }
                ConnectState.NOT_CONNECTED -> {
                    binding.tvConnection.text = getString(R.string.disconnected)
                }
                ConnectState.NO_DEVICE -> {
                    binding.tvConnection.text = getString(R.string.no_selected_device)
                }
                ConnectState.DEVICE_SELECTED -> {
                    binding.tvConnection.text = getString(R.string.connecting)
                }
            }
        }
    }

    private fun loadDbList() {
        /*val storage = Firebase.storage
        val listRef = storage.reference.child("Kinder")
        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.
                }

                items.forEach { item ->
                    dbList = ArrayList()
                    dbList.add(item.toString())
                    adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
                    binding.autoCompleteTextView.setAdapter(adapter)
                }
            }
            .addOnFailureListener {
                // Uh-oh, an error occurred!
            }*/

        //val storageRef = FirebaseStorage.getInstance().reference.child("$useSelected/$dicedSide.jpg")
        //Liste aus Datenbank
        db.collection("Verwendungszweck")
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    return@EventListener
                }
                updateListOnChange(value!!)
            })

    }

    //Liste aus Datenbank
    private fun updateListOnChange(value: QuerySnapshot) {
        dbList = ArrayList()
        for (documentSnapshot in value) {
            val highscore = documentSnapshot.toObject(Data::class.java)
            dbList.add(highscore.toString())
        }
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
        binding.autoCompleteTextView.setAdapter(adapter)
    }


    private fun showDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.fabAnleitung)
                .setMessage(R.string.anleitung)
                .setPositiveButton(R.string.dialog_cancel) { dialog, which ->
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}