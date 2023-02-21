package com.example.cubegameapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuidFrom
import com.example.cubegameapp.esp32ble.*
import com.juul.kable.Filter
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

object ScanState {
    const val NOT_SCANNING = 0
    const val SCANNING = 1
    const val FAILED = 2
}

object ConnectState {
    const val NO_DEVICE = 0
    const val DEVICE_SELECTED = 1
    const val CONNECTED = 2
    const val NOT_CONNECTED = 3
}

data class Device(val name: String, val address: String) {
    override fun toString(): String = name + ": " + address
}

class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"


    //---Device auswählen---
    private val _deviceList = MutableLiveData<MutableList<Device>>()
    val deviceList: LiveData<MutableList<Device>>
        get() = _deviceList


    fun getDeviceList(): List<Device>? {
        return _deviceList.value
    }


    private var deviceSelected = "CubeGame1: 24:6F:28:1A:71:76"
    fun getDeviceSelected(): String {
        return deviceSelected
    }


    fun setDeviceSelected(devicestring: String) {
        deviceSelected = devicestring
        _connectState.value = ConnectState.DEVICE_SELECTED
    }


    //---Zweck auswhählen---
    private val _selectedUse = MutableLiveData<String>()
    val selectedUse: LiveData<String>
        get() = _selectedUse


    fun getUseSelected(): String {
        return _selectedUse.value.toString()
    }

    fun setUseSelected(use: String) {
        _selectedUse.value = use
        Log.i(TAG,_selectedUse.value.toString())
    }


    //---ausgewählte Runden---
    private var _roundsSelected = MutableLiveData<Int>()
    val roundsSelected: LiveData<Int>
        get() = _roundsSelected

    fun setRoundsSelected(rounds: Int) {
        _roundsSelected.value = rounds
    }

    fun getRoundsSelected(): String {
        val rounds = _roundsSelected.value
        return rounds.toString()
    }


    //---Rundenanzahl hochrechnen---
    private var _btnCounter = MutableLiveData<Int>()
    val btnCounter: LiveData<Int>
        get() = _btnCounter

    fun incBtnCounter() {
        _btnCounter.value = (_btnCounter.value ?: 0) + 1
        Log.i(TAG, "btnCounter = ${_btnCounter.value}")
    }

    fun resetBtnCounter() {
        _btnCounter.value = 0
        Log.i(TAG, "BtnCounter zurückgesetzt")
    }


    //---Punktezahl hochrechnen---
    private var _counterA = MutableLiveData<Int>()
    val counterA: LiveData<Int>
        get() = _counterA

    fun incCounterA() {
        _counterA.value = (_counterA.value ?: 0) + 1
        Log.i(TAG, "counterA = ${_counterA.value}")
    }

    fun resetCounterA() {
        _counterA.value = 0
        Log.i(TAG, "counterA zurückgesetzt")
    }

    private var _counterB = MutableLiveData<Int>()
    val counterB: LiveData<Int>
        get() = _counterB

    fun incCounterB() {
        _counterB.value = (_counterB.value ?: 0) + 1
        Log.i(TAG, "counterB = ${_counterB.value}")
    }

    fun resetCounterB() {
        _counterB.value = 0
        Log.i(TAG, "counterB zurückgesetzt")
    }


    //---Boolean---
    private val _booleanNext = MutableLiveData<Boolean>(false)
    val booleanNext: LiveData<Boolean>
        get() = _booleanNext

    fun switchBoolean() {
        _booleanNext.value?.let {
            _booleanNext.value = !it
        }
        Log.i(TAG, _booleanNext.toString())
    }


    //---Liste Spieler---
    private val _playerList = MutableLiveData<MutableList<String>>()
    val playerList: LiveData<MutableList<String>>
        get() = _playerList

    fun getPlayerList(): List<String>? {
        return _playerList.value
    }


    //---Liste ausgewählte Spieler---
    private val _selectedPlayerListA = MutableLiveData<MutableList<String>>()
    val selectedPlayerListA: LiveData<MutableList<String>>
        get() = _selectedPlayerListA

    fun getSelectedPlayerListA(): List<String>? {
        return _selectedPlayerListA.value
    }

    private val _selectedPlayerListB = MutableLiveData<MutableList<String>>()
    val selectedPlayerListB: LiveData<MutableList<String>>
        get() = _selectedPlayerListB

    fun getSelectedPlayerListB(): List<String>? {
        return _selectedPlayerListB.value
    }


    init {
        _deviceList.value = mutableListOf()
        _selectedUse.value = ""
        _btnCounter.value = 1
        _playerList.value = mutableListOf()
        _selectedPlayerListA.value = mutableListOf()
        _selectedPlayerListB.value = mutableListOf()
        //_playerList.value = mutableListOf("Testtermin 1", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2", "Testtermin 2")
    }

    fun addPlayer(player: String) {
        if (!(_playerList.value?.contains(player) ?: true)) {
            _playerList.value?.add(player)
            _playerList.notifyObserver()
            Log.i("MVM Add:", _playerList.value.toString())
        }
    }

    fun deletePlayer(player: String) {
        _playerList.value?.removeAll() {it.equals(player)}
        _playerList.notifyObserver()
        Log.i("MVM Delete:", _playerList.value.toString())
    }

    fun emptySelectedPlayers(player: String) {
        _selectedPlayerListA.value?.removeAll() {it.equals(player)}
        _selectedPlayerListB.value?.removeAll() {it.equals(player)}
        _selectedPlayerListA.notifyObserver()
        _selectedPlayerListB.notifyObserver()
        Log.i("MVM Empty","done")
    }

    fun selectPlayerA(player: String) {
        _selectedPlayerListA.value?.add(player)
        _selectedPlayerListA.notifyObserver()
        Log.i("MVM Select A:", _selectedPlayerListA.value.toString())
    }

    fun selectPlayerB(player: String) {
        _selectedPlayerListB.value?.add(player)
        _selectedPlayerListB.notifyObserver()
        Log.i("MVM Select B:", _selectedPlayerListB.value.toString())
    }


    // Scanning
    // ------------------------------------------------------------------------------

    private lateinit var scanJob: Job

    private val scanner = Scanner {
        filters = listOf(
            Filter.Service(uuidFrom(CUSTOM_SERVICE_UUID))
        )
    }

    private var scanState = ScanState.NOT_SCANNING

    fun startScan() {
        Log.i(">>>>", "Start Scanning ...")
        if (scanState == ScanState.SCANNING) return // Scan already in progress.
        scanState = ScanState.SCANNING

        val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)
        scanJob = viewModelScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner
                    .advertisements
                    //.filter { advertisement -> advertisement.name?.startsWith("CubeGame1") == true }
                    .catch {cause -> scanState = ScanState.FAILED
                        Log.i(">>>> Scanning Failed", cause.message.toString())
                    }
                    .onCompletion { cause -> if (cause == null || cause is CancellationException)
                        scanState = ScanState.NOT_SCANNING
                    }
                    .collect { advertisement ->
                        val device = Device(name = advertisement.name.toString(),
                            address = advertisement.address.toString())
                        //deviceSelected = device.toString()
                        setDeviceSelected(device.toString())
                        //Log.i(">>deviceSelected>>:", deviceSelected)
                        /*if (_deviceList.value?.contains(device) == false) {
                            _deviceList.value?.add(device)
                            _deviceList.notifyObserver()
                        }*/
                        //Log.i(">>>>", _deviceList.value.toString())
                        //Log.i(">>>>>", deviceSelected)
                    }
            }
        }
    }

    fun stopScan() {
        scanState = ScanState.NOT_SCANNING
        scanJob.cancel()
    }

    // Connecting
    // --------------------------------------------------------------------------

    private lateinit var peripheral: Peripheral
    private lateinit var esp32: Esp32Ble

    private val _connectState = MutableLiveData<Int>(ConnectState.NO_DEVICE)
    val connectState: LiveData<Int>
        get() = _connectState

    fun connect() {
        if (_connectState.value == ConnectState.NO_DEVICE) return
        val macAddress = deviceSelected.substring(deviceSelected.length -17)
        //val macAddress = "24:6F:28:1A:71:76"
        Log.i("macAdress:", macAddress)
        peripheral = viewModelScope.peripheral(macAddress) {
            onServicesDiscovered {
                requestMtu(517)
            }
        }
        esp32 = Esp32Ble(peripheral)


        viewModelScope.launch {
            peripheral.state.collect { state ->

                Log.i(">>>> Connection State:", state.toString())
                when (state.toString()) {
                    "Connected" -> _connectState.value = ConnectState.CONNECTED
                    "Disconnected(null)" -> _connectState.value = ConnectState.NOT_CONNECTED
                    else -> _connectState.value = ConnectState.NOT_CONNECTED
                }
            }
        }

        viewModelScope.launch {
            esp32.connect()
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            // Allow 5 seconds for graceful disconnect before forcefully closing `Peripheral`.
            withTimeoutOrNull(5_000L) {
                esp32.disconnect()
            }
        }
    }



    // Extension Function, um Änderung in den Einträgen von Listen
    // dem Observer anzeigen zu können
    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }


    // Communication
    // ____________________________________________________________________


    //var ledData = LedData()
    var pData = Play()
    var sData = Stop()
    var repeatData = Repeat()
    var round = Round()
    var gameStatus = GameStatus()

    private lateinit var dataLoadJob: Job

    private var _esp32Data = MutableLiveData<Esp32Data>(Esp32Data())
    val esp32Data: LiveData<Esp32Data>
        get() = _esp32Data

    fun startDataLoadJob() {
        dataLoadJob = viewModelScope.launch {
            esp32.incomingMessages.collect { msg ->
                val jsonstring = String(msg)
                Log.i(">>>> msg in", jsonstring)
                _esp32Data.value = jsonParseEsp32Data(jsonstring)
            }
        }
    }

    fun cancelDataLoadJob() {
        dataLoadJob.cancel()
        Log.i(TAG, "CancelDataLoadjob")
    }

    //Daten Spielen
    fun sendDataPlay() {
        viewModelScope.launch {
            try {
                esp32.sendMessage(jsonEncodePlayData(pData))
            } catch (e:Exception) {
                Log.i(">>>>>", "Error sending pData ${e.message}" + e.toString())
            }
        }
    }

    fun sendDataStop() {
        viewModelScope.launch {
            try {
                esp32.sendMessage(jsonEncodeStopData(sData))
            } catch (e:Exception) {
                Log.i(">>>>>", "Error sending pData ${e.message}" + e.toString())
            }
        }
    }

    //Daten wiederholen
    fun sendDataRepeat() {
        viewModelScope.launch {
            try {
                esp32.sendMessage(jsonEncodeRepeat(repeatData))
            } catch (e:Exception) {
                Log.i(">>>>>", "Error sending pData ${e.message}" + e.toString())
            }
        }
    }

    fun sendRoundData(selectedItem: Int) {
        viewModelScope.launch {
            try {
                //esp32.sendMessage(jsonEncodeRound(round))
                esp32.sendMessage(jsonEncodeRound(selectedItem))
            } catch (e:Exception) {
                Log.i(">>>>>", "Error sending pData ${e.message}" + e.toString())
            }
        }
    }

    fun sendGameStatus() {
        viewModelScope.launch {
            try {
                esp32.sendMessage(jsonEncodeGameStatus(gameStatus))
            } catch (e: Exception) {
                Log.i(">>>>>", "Error sending ledData ${e.message}" + e.toString())
            }
        }
    }

    private fun jsonEncodeGameStatus(gameStatus: GameStatus): String {
        val obj = JSONObject()
        obj.put("GameStatus", gameStatus.gameStatus)
        return obj.toString()
    }

    /*fun sendLedData() {
        viewModelScope.launch {
            try {
                esp32.sendMessage(jsonEncodeLedData(ledData))
            } catch (e: Exception) {
                Log.i(">>>>>", "Error sending ledData ${e.message}" + e.toString())
            }
        }
    }*/

    private fun jsonEncodeRepeat(repeat: Repeat): String {
        val obj = JSONObject()
        obj.put("REPEAT", repeat.repeat)
        return obj.toString()
    }

    private fun jsonEncodePlayData(pData: Play): String {
        val obj = JSONObject()
        obj.put("PLAY", pData.play)
        return obj.toString()
    }

    private fun jsonEncodeStopData(sData: Stop): String {
        val obj = JSONObject()
        obj.put("STOP", sData.stop)
        return obj.toString()
    }

    private fun jsonEncodeRound(selectedItem: Int): String {
        val obj = JSONObject()
        obj.put("ROUND", selectedItem)
        return obj.toString()
    }

    /*private fun jsonEncodeLedData(ledData: LedData): String {
        val obj = JSONObject()
        obj.put("LED", ledData.led)
        obj.put("LEDBlinken", ledData.ledBlinken)
        return obj.toString()
    }*/

    fun jsonParseEsp32Data(jsonString: String): Esp32Data {
        try {
            val obj = JSONObject(jsonString)
            return Esp32Data(
                playStatus = obj.getString("playStatus"),
                seite = obj.getString("seite"),
                //ledstatus = obj.getString("ledstatus"),
                //potiArray = obj.getJSONArray("potiArray")
            )
        } catch (e: Exception) {
            Log.i(">>>>", "Error decoding JSON ${e.message}")
            return Esp32Data()
        }
    }
}