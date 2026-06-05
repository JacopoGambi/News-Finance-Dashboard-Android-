package com.example.newsfinance.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val preferredCurrency: String = "usd",
    val notificationsEnabled: Boolean = true,
    val updateIntervalMinutes: Int = 30,
    // Lingua scelta per le notizie. Vuota = segui la lingua di sistema.
    val preferredLang: String = "",
    // Tema dell'app: "system" | "light" | "dark"
    val themeMode: String = "system"
)

/**
 * Astrazione sulle preferenze utente: consente di sostituirla con un fake nei test
 * e mantiene i consumer (ViewModel, repository, worker) indipendenti da DataStore.
 */
interface UserPreferencesDataStore {
    val preferences: Flow<UserPreferences>
    suspend fun updateCurrency(currency: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateIntervalMinutes(minutes: Int)
    suspend fun updateLang(lang: String)
    suspend fun updateThemeMode(mode: String)
}

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {
    private object Keys {
        val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val UPDATE_INTERVAL_MINUTES = intPreferencesKey("update_interval_minutes")
        val PREFERRED_LANG = stringPreferencesKey("preferred_lang")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            preferredCurrency = prefs[Keys.PREFERRED_CURRENCY] ?: "usd",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            updateIntervalMinutes = prefs[Keys.UPDATE_INTERVAL_MINUTES] ?: 30,
            preferredLang = prefs[Keys.PREFERRED_LANG] ?: "",
            themeMode = prefs[Keys.THEME_MODE] ?: "system"
        )
    }

    override suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { it[Keys.PREFERRED_CURRENCY] = currency }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun updateIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.UPDATE_INTERVAL_MINUTES] = minutes }
    }

    override suspend fun updateLang(lang: String) {
        context.dataStore.edit { it[Keys.PREFERRED_LANG] = lang }
    }

    override suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }
}
