package com.example.news.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.news.domain.manager.LocalUserManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Top-level delegate: DataStore must be a single instance per file for the whole process. */
private val Context.userSettings: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class LocalUserManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalUserManager {

    override suspend fun saveAppEntry() {
        context.userSettings.edit { it[APP_ENTRY] = true }
    }

    override fun readAppEntry(): Flow<Boolean> =
        context.userSettings.data.map { it[APP_ENTRY] ?: false }

    private companion object {
        /** true once the user finished onboarding. */
        val APP_ENTRY = booleanPreferencesKey("app_entry")
    }
}
