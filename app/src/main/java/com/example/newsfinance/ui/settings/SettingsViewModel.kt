package com.example.newsfinance.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.data.local.UserPreferences
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.worker.PriceAlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsStore: UserPreferencesDataStore,
    private val priceAlertScheduler: PriceAlertScheduler
) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = prefsStore.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun onCurrencyChanged(currency: String) {
        viewModelScope.launch { prefsStore.updateCurrency(currency) }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch {
            prefsStore.updateNotificationsEnabled(enabled)
            if (enabled) {
                val interval = prefsStore.preferences.first().updateIntervalMinutes.toLong()
                priceAlertScheduler.schedule(interval)
            } else {
                priceAlertScheduler.cancel()
            }
        }
    }

    fun onIntervalChanged(minutes: Int) {
        viewModelScope.launch {
            prefsStore.updateIntervalMinutes(minutes)
            // Rischedula solo se le notifiche sono attive
            if (prefsStore.preferences.first().notificationsEnabled) {
                priceAlertScheduler.schedule(minutes.toLong())
            }
        }
    }

    fun onLanguageChanged(lang: String) {
        viewModelScope.launch { prefsStore.updateLang(lang) }
    }
}
