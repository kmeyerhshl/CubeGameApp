package com.example.cubegameapp.esp32ble

import org.json.JSONArray

data class LedData(var led: String = "L", var ledBlinken: Boolean = false)
//data class Esp32Data(val ledstatus: String = "", val potiArray: JSONArray = JSONArray())

data class Play(var play: String = "P")
data class Stop(var stop: String = "S")
data class GameStatus(var gameStatus: String = "")
data class Esp32Data(val playStatus: String = "", val seite: String = "")
data class Repeat(var repeat: String = "R")
data class Round(var roundnr: Int = 0)