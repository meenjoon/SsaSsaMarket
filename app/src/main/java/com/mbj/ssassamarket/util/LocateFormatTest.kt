package com.mbj.ssassamarket.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LocateFormatTest {

    @Test
    fun testGetSelectedAddress_withThreeWords() {
        val address = "대전 유성구 봉명동 608"
        val numWords = 3

        val selectedAddress = LocateFormat.getSelectedAddress(address, numWords)

        assertEquals("대전 유성구 봉명동", selectedAddress)
    }

    @Test
    fun testGetSelectedAddress_withTwoWords() {
        val address = "대전 유성가구 봉명동 608"
        val numWords = 2

        val selectedAddress = LocateFormat.getSelectedAddress(address, numWords)

        assertEquals("대전 유성구", selectedAddress)
    }

    @Test
    fun testGetSelectedAddress_withOneWord() {
        val address = "대전 유성구 봉명동 608"
        val numWords = 1

        val selectedAddress = LocateFormat.getSelectedAddress(address, numWords)

        assertEquals("대전", selectedAddress)
    }

    @Test
    fun testGetSelectedAddress_withZeroWords() {
        val address = "대전 유성구 봉명동 608"
        val numWords = 0

        val selectedAddress = LocateFormat.getSelectedAddress(address, numWords)

        assertEquals("", selectedAddress)
    }

    @Test
    fun testGetSelectedAddress_withNegativeNumWords() {
        val address = "대전 유성구 봉명동 608"
        val numWords = -1

        val selectedAddress = LocateFormat.getSelectedAddress(address, numWords)

        assertEquals("", selectedAddress)
    }
}
