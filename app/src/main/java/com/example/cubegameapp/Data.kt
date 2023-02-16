package com.example.cubegameapp

class Data {

    private var use: String? = null

    fun getUse(): String? {
        return use
    }

    fun setUse(use: String?) {
        this.use = use
    }

    override fun toString(): String {
        return "Zweck: " + use
    }

}