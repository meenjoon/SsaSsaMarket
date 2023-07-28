package com.mbj.ssassamarket.data.source.local

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class ProductTypeConverters {

    private val moshi: Moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, String::class.java)
    private val jsonAdapter: JsonAdapter<List<String>> = moshi.adapter(type)

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return jsonAdapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        return try {
            val lenientJsonAdapter = jsonAdapter.lenient()
            lenientJsonAdapter.fromJson(value)
        } catch (e: JsonDataException) {
            null
        }
    }
}
