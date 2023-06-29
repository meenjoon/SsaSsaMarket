package com.mbj.ssassamarket.util

object LocateFormat {

    fun getSelectedAddress(address: String, numWords: Int): String {
        val addressParts = address.split(" ")
        val selectedParts = addressParts.take(numWords)
        return selectedParts.joinToString(" ")
    }
}
