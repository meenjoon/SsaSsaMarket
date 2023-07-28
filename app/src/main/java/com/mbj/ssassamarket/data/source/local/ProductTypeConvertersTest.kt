package com.mbj.ssassamarket.data.source.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ProductTypeConvertersTest {

    private lateinit var productTypeConverters: ProductTypeConverters

    @Before
    fun setUp() {
        productTypeConverters = ProductTypeConverters()
    }

    @Test
    fun testFromStringList() {
        val list = listOf("a", "b", "c")

        val jsonString = productTypeConverters.fromStringList(list)

        assertEquals("""["a","b","c"]""", jsonString)
    }

    @Test
    fun testFromStringList_withNullList() {
        val list: List<String>? = null

        val jsonString = productTypeConverters.fromStringList(list)

        assertEquals("null", jsonString)
    }

    @Test
    fun testToStringList() {
        val jsonString = """["a","b","c"]"""

        val list = productTypeConverters.toStringList(jsonString)

        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test
    fun testToStringList_withInvalidJson() {
        val jsonString = """invalid_json_string"""

        val list = productTypeConverters.toStringList(jsonString)

        assertNull(list)
    }
}
