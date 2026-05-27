package com.example.newsfinance.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.data.local.UserPreferences
import com.example.newsfinance.data.local.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = prefsStore.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun onCurrencyChanged(currency: String) {
        viewModelScope.launch { prefsStore.updateCurrency(currency) }
    }

    fun onCountryChanged(country: String) {
        viewModelScope.launch { prefsStore.updateCountry(country) }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        viewModelScope.launch { prefsStore.updateNotificationsEnabled(enabled) }
    }

    fun onIntervalChanged(minutes: Int) {
        viewModelScope.launch { prefsStore.updateIntervalMinutes(minutes) }
    }
}
