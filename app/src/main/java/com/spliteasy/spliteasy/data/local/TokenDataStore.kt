package com.spliteasy.spliteasy.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenDataStore(private val context: Context) {
    private val KEY_ACTIVE_GROUP = stringPreferencesKey("active_group")
    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_ROLE  = stringPreferencesKey("role")

    val tokenFlow = context.dataStore.data.map { it[KEY_TOKEN] }
    val roleFlow  = context.dataStore.data.map { it[KEY_ROLE]  }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }
    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs -> prefs[KEY_ROLE]  = role }
    }
    suspend fun save(token: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_ROLE]  = role
        }
    }
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun saveActiveGroupId(id: Long) {
        context.dataStore.edit { it[KEY_ACTIVE_GROUP] = id.toString() }
    }

    suspend fun readActiveGroupId(): Long? =
        context.dataStore.data.first()[KEY_ACTIVE_GROUP]?.toLongOrNull()

    suspend fun readToken(): String? = tokenFlow.first()
    suspend fun readRole(): String?  = roleFlow.first()
}
