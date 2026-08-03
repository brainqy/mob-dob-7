package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jobtraq_session")

data class SessionData(
    val authToken: String?,
    val userEmail: String?,
    val userId: Long?,
    val locale: String,
    val rememberMe: Boolean,
    val tenantId: String
)

class SessionManager(private val context: Context) {

    private object Keys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ID = stringPreferencesKey("user_id")
        val LOCALE = stringPreferencesKey("locale")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val TENANT_ID = stringPreferencesKey("tenant_id")
    }

    val sessionFlow: Flow<SessionData> = context.dataStore.data.map { prefs ->
        SessionData(
            authToken = prefs[Keys.AUTH_TOKEN]?.ifBlank { null },
            userEmail = prefs[Keys.USER_EMAIL],
            userId = prefs[Keys.USER_ID]?.toLongOrNull(),
            locale = prefs[Keys.LOCALE] ?: "en",
            rememberMe = prefs[Keys.REMEMBER_ME] ?: false,
            tenantId = prefs[Keys.TENANT_ID] ?: "platform"
        )
    }

    suspend fun currentSession(): SessionData = sessionFlow.first()

    suspend fun saveLoginSession(
        token: String?,
        email: String,
        userId: Long,
        rememberMe: Boolean,
        tenantId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = token.orEmpty()
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_ID] = userId.toString()
            prefs[Keys.REMEMBER_ME] = rememberMe
            prefs[Keys.TENANT_ID] = tenantId
        }
    }

    suspend fun clearSession() {
        val current = currentSession()
        context.dataStore.edit { prefs ->
            if (current.rememberMe) {
                prefs.remove(Keys.AUTH_TOKEN)
                prefs.remove(Keys.USER_ID)
            } else {
                prefs.remove(Keys.AUTH_TOKEN)
                prefs.remove(Keys.USER_EMAIL)
                prefs.remove(Keys.USER_ID)
                prefs.remove(Keys.REMEMBER_ME)
                prefs.remove(Keys.TENANT_ID)
            }
        }
    }

    suspend fun setLocale(locale: String) {
        context.dataStore.edit { it[Keys.LOCALE] = locale }
    }

    suspend fun setTenantId(tenantId: String) {
        context.dataStore.edit { it[Keys.TENANT_ID] = tenantId }
    }

    companion object {
        const val DEFAULT_LOCALE = "en"
        val SUPPORTED_LOCALES = listOf("en", "hi", "mr")
    }
}
