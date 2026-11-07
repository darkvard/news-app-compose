package com.example.news.presentation.navgraph

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/** NavType for passing any @Serializable class as a type-safe navigation argument. */
inline fun <reified T : Any> serializableNavType(json: Json = Json) =
    object : NavType<T>(isNullableAllowed = false) {
        override fun get(bundle: Bundle, key: String): T? =
            bundle.getString(key)?.let { json.decodeFromString<T>(it) }

        override fun parseValue(value: String): T = json.decodeFromString(value)

        override fun serializeAsValue(value: T): String = Uri.encode(json.encodeToString(serializer<T>(), value))

        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putString(key, json.encodeToString(serializer<T>(), value))
        }
    }
