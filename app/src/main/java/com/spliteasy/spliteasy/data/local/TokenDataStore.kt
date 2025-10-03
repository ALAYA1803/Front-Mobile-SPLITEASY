package com.spliteasy.spliteasy.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_ROLE  = stringPreferencesKey("role")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }
    val roleFlow:  Flow<String?> = context.dataStore.data.map { prefs -> prefs[KEY_ROLE]  }

    suspend fun save(token: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_ROLE]  = role
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs -> prefs[KEY_ROLE] = role }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
