package com.spliteasy.spliteasy.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val KEY_ACTIVE_GROUP = stringPreferencesKey("active_group")
    private val KEY_TOKEN        = stringPreferencesKey("access_token")
    private val KEY_ROLE         = stringPreferencesKey("role")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val roleFlow:  Flow<String?> = context.dataStore.data.map { it[KEY_ROLE]  }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs -> prefs[KEY_ROLE] = role }
    }

    suspend fun save(token: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_ROLE] = role
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

    suspend fun readToken(): String? = context.dataStore.data.first()[KEY_TOKEN]

    suspend fun readRole(): String? = context.dataStore.data.first()[KEY_ROLE]
}
