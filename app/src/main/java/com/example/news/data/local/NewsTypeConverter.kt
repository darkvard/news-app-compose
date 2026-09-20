package com.example.news.data.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.news.domain.model.Source
import com.google.gson.Gson

@ProvidedTypeConverter
class NewsTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun sourceToString(source: Source): String {
        return gson.toJson(source)
    }

    @TypeConverter
    fun stringToSource(json: String): Source {
        return gson.fromJson(json, Source::class.java)
    }
}