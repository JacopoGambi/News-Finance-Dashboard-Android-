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
    val preferredCountry: String = "us",
    val notificationsEnabled: Boolean = true,
    val updateIntervalMinutes: Int = 30
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
        val PREFERRED_COUNTRY = stringPreferencesKey("preferred_country")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val UPDATE_INTERVAL_MINUTES = intPreferencesKey("update_interval_minutes")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            preferredCurrency = prefs[Keys.PREFERRED_CURRENCY] ?: "usd",
            preferredCountry = prefs[Keys.PREFERRED_COUNTRY] ?: "us",
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            updateIntervalMinutes = prefs[Keys.UPDATE_INTERVAL_MINUTES] ?: 30
        )
    }

    suspend fun updateCurrency(currency: String) {
        context.dataStore.edit { it[Keys.PREFERRED_CURRENCY] = currency }
    }

    suspend fun updateCountry(country: String) {
        context.dataStore.edit { it[Keys.PREFERRED_COUNTRY] = country }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun updateIntervalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.UPDATE_INTERVAL_MINUTES] = minutes }
    }
}
