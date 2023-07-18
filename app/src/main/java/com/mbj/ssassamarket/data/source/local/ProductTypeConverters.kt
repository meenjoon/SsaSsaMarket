package com.mbj.ssassamarket.data.source.local

import androidx.room.TypeConverter

class ProductTypeConverters {

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return value.split(",")
    }
}
