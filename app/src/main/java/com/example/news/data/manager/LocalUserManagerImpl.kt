package com.example.news.data.manager

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.news.domain.manager.LocalUserManger
import com.example.news.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserManagerImpl @Inject constructor (
    private val application: Application
) : LocalUserManger {

    private val readOnlyProperty = preferencesDataStore(name = Constants.USER_SETTINGS)

    val Context.dataStore: DataStore<Preferences> by readOnlyProperty

    override suspend fun saveAppEntry() {
        application.dataStore.edit {
            it[PreferenceKeys.APP_ENTRY] = true
        }
    }

    override fun readAppEntry(): Flow<Boolean> {
        return application.dataStore.data.map {
            it[PreferenceKeys.APP_ENTRY] ?: false
        }
    }

    private object PreferenceKeys {
        val APP_ENTRY = booleanPreferencesKey(Constants.APP_ENTRY)
    }

}