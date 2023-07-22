package com.mbj.ssassamarket.data.source.local

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class ProductTypeConverters {

    private val moshi = Moshi.Builder().build()

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter(type)
        return jsonAdapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter(type)
        return try {
            jsonAdapter.fromJson(value)
        } catch (e: JsonDataException) {
            null
        }
    }
}
