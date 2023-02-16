package com.example.cubegameapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.example.cubegameapp.databinding.FragmentHomeBinding
import com.example.cubegameapp.model.ConnectState
import com.example.cubegameapp.model.MainViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private val TAG = "Home"

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private val mFirebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var dbList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    //private var listDB: ArrayList<String>


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
            Log.i("ButtonFirst", "[btnStart] isReceivingData - true")
            viewModel.connect()
        }

        binding.textviewFirst.text = viewModel.getDeviceSelected()

        //val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        //val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        //binding.autoCompleteTextView.setAdapter(adapter)

        loadDbList()

        binding.btnMenu.setOnClickListener {
            Log.i(TAG, "Button1")
            //useSelected = button1
            //viewModel.setUseSelected(useSelected)
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
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
        /*val documentID = listDB.toString()
        db.collection("Verwendungszweck").document(documentID)
            .get()
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    dbList = ArrayList()
                    val highscore = task.result!!.toObject(Data::class.java)
                    dbList.add(highscore.toString())

                    adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
                    binding.autoCompleteTextView.setAdapter(adapter)
                } else {
                    Log.d(TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }*/
        val uid = mFirebaseAuth.currentUser!!.uid
        db.collection("Verwendungszweck")
            //.orderBy(Constants.USERSCORE, Query.Direction.DESCENDING)
            //.limit(Constants.HIGHSCORELIMIT.toLong())
            .addSnapshotListener(EventListener { value, e ->
                if (e != null) {
                    return@EventListener
                }
                updateListOnChange(value!!)
            })
        
        // Einstiegspunkt für die Abfrage ist users/uid/Messungen
        //val uid = mFirebaseAuth.currentUser!!.uid
        /*db.collection("Verwendungszweck").document()
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateListView(task)
                } else {
                    Log.d(TAG, "FEHLER: Daten lesen ", task.exception)
                }
            }*/
        /*db.collection("Verwendungszweck") // alle Einträge abrufen
            //.get()
            .addSnapshotListener(EventListener { task, e ->
                if (e != null) {
                    return@EventListener
                }
                updateListView(task!!)
                /*if (task.isSuccessful) {
                    updateListView(task!!)
                } else {
                    Log.d(TAG, "FEHLER: Daten lesen ", task.exception)
                }*/
            })*/
    }

    private fun updateListOnChange(value: QuerySnapshot) {
        dbList = ArrayList()
        for (documentSnapshot in value) {
            val highscore = documentSnapshot.toObject(Data::class.java)
            dbList.add(highscore.toString())
        }
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
        binding.autoCompleteTextView.setAdapter(adapter)
    }

    /*private fun updateListView(task: Task<QuerySnapshot>){
        Log.i(TAG, "updateListView")
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen
        dbList = ArrayList()
        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task.result!!) {
            val messung = document.toObject(Data::class.java)
            Log.i(TAG, "Messung: $messung")
            //val id = document.id
            //messung.setId(id)
            (dbList as ArrayList<Data>).add(messung)
            Log.d(TAG, document.id + " => " + document.data)
        }
        // jetzt liegt die vollständige Liste vor und
        // kann im ListView angezeigt werden
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
        //listView.adapter = adapter
        binding.autoCompleteTextView.setAdapter(adapter)
    }*/

    /*private fun updateListView(task: QuerySnapshot) {
        Log.i(TAG, "updateListView")
        // Einträge in dbList kopieren, um sie im ListView anzuzeigen
        dbList = ArrayList()
        // Diese for schleife durchläuft alle Documents der Abfrage
        for (document in task) {
            val messung = document.toObject(Data::class.java)
            Log.i(TAG, "Messung: $messung")
            //val id = document.id
            //messung.setId(id)
            (dbList as ArrayList<Data>).add(messung)
            Log.d(TAG, document.id + " => " + document.data)
        }
        // jetzt liegt die vollständige Liste vor und
        // kann im ListView angezeigt werden
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, dbList)
        //listView.adapter = adapter
        binding.autoCompleteTextView.setAdapter(adapter)
    }*/

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