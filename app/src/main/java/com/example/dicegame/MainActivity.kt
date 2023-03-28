package com.example.dicegame

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.PermissionChecker
import com.example.dicegame.databinding.ActivityMainBinding
import com.example.dicegame.model.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import splitties.alertdialog.alertDialog
import splitties.alertdialog.negativeButton
import splitties.alertdialog.positiveButton
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    lateinit var wifiManager: WifiManager

    private lateinit var auth: FirebaseAuth

    private val viewModel: MainViewModel by viewModels()

    private val LISTSIZE = "listsize"
    private val LISTITEM = "item_"
    private val SELECTEDITEM ="selected"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBTPermission()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //ist das Handy mit dem Internet verbunden?
        fun  isNetworkAvailabale():Boolean{
            val conManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val internetInfo =conManager.activeNetworkInfo
            return internetInfo!=null && internetInfo.isConnected
        }

        //wenn kein Internet verfügbar ist
        if(! isNetworkAvailabale()){
            alertDialog (title = getString(R.string.request_wifi_enable)) {
                positiveButton(R.string.dialog_ok){
                    try {
                        //WLAN anschalten
                        // for android Q and above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val panelIntent: Intent =
                                Intent(android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                            startActivityForResult(panelIntent, 0)
                        } else {
                            // for previous android version
                            wifiManager.isWifiEnabled = true
                        }
                    }catch (e : Exception){
                        toast(getString(R.string.finding_wifi_failed_please_create_connection))
                        finish()
                    }
                }
                negativeButton(R.string.negButton){
                    toast(getString(R.string.please_connect_internet))
                    finish()
                }
            }.show()
        }

        //Firebase
        auth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (currentUser != null) {
            Log.i(TAG,"Eingeloggt")
        } else {
            signIn()
        }

        readSharedPreferences()


    }

    private fun signIn() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        Log.i(TAG,"updateUI")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
        writeSharedPreferences()
    }

    private fun writeSharedPreferences() {
        Log.i(TAG, "writeSharedPreferences")
        // speicher die Spielerliste
        val sp = getPreferences(Context.MODE_PRIVATE)
        val edit = sp.edit()
        val list = viewModel.getPlayerList()!!
        edit.putInt(LISTSIZE, list.size)
        for(i in 0 until list.size){
            edit.putString("$LISTITEM$i", list.get(i))
        }
        edit.commit()
    }

    private fun readSharedPreferences() {
        Log.i(TAG, "readSharedPreferences")
        // Spieler wieder einlesen
        val sp = getPreferences(Context.MODE_PRIVATE)
        val anzahl = sp.getInt(LISTSIZE, 0)
        for(i in 0 until anzahl){
            viewModel.addPlayer(sp.getString("$LISTITEM$i", "").toString())
        }
    }

    private fun checkBTPermission() {
        var permissionCheck = PermissionChecker.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION")
        permissionCheck += PermissionChecker.checkSelfPermission(this, "Manifest.permission.ACCESS_COARSE_LOCATION")
        permissionCheck += PermissionChecker.checkSelfPermission(this, "Manifest.permission.BLUETOOTH_CONNECT")
        permissionCheck += PermissionChecker.checkSelfPermission(this, "Manifest.permission.BLUETOOTH_SCAN")
        if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN), 1001)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        val bluetooth = BluetoothAdapter.getDefaultAdapter()

        // ist BT auf dem Device verfügbar?
        if(bluetooth == null)
        {
            Toast.makeText(this, getString(R.string.bt_not_available), Toast.LENGTH_LONG).show()
            finish();
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, getString(R.string.ble_not_supported), Toast.LENGTH_LONG).show()
            finish()
        }
        // ist BT eingeschaltet?
        if (!bluetooth.isEnabled) {
            val turnBTOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTOn, 1)
        }
    }
}