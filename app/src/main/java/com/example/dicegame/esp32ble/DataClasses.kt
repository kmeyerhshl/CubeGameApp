package com.example.dicegame.esp32ble

import android.util.Log
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf

const val CUSTOM_SERVICE_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB"
const val CUSTOM_CHARACTERISTIC_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB"

private val CustomConfigCharacteristic = characteristicOf(
    service = CUSTOM_SERVICE_UUID,
    characteristic = CUSTOM_CHARACTERISTIC_UUID
)




class Esp32Ble(private val peripheral: Peripheral): Peripheral by peripheral {

    val incomingMessages = peripheral.observe(CustomConfigCharacteristic)

    suspend fun sendMessage(msg: String) {
        peripheral.write(CustomConfigCharacteristic, msg.toByteArray(), WriteType.WithResponse)
        Log.i(">>>> sending", msg)
    }
}