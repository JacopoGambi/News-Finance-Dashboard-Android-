package com.example.newsfinance.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.usecase.GetHomeDataUseCase
import com.example.newsfinance.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val cryptos: List<Crypto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _currentCountry = MutableStateFlow("us")

    // True quando il paese è stato impostato dalla posizione GPS; in tal caso
    // le modifiche alle preferenze non sovrascrivono l'override di sessione.
    private var locationCountryActive = false

    private var loadJob: Job? = null

    init {
        // Aggiorna il paese dalle preferenze utente (solo se non c'è override GPS attivo)
        viewModelScope.launch {
            prefsStore.preferences.collect { prefs ->
                if (!locationCountryActive) {
                    _currentCountry.value = prefs.preferredCountry
                }
            }
        }
        // Ricarica i dati ogni volta che il paese corrente cambia
        viewModelScope.launch {
            _currentCountry.collect { country ->
                loadData(country)
            }
        }
    }

    fun refresh() {
        loadData(_currentCountry.value)
    }

    fun setUserCountry(countryCode: String) {
        locationCountryActive = true
        _currentCountry.value = countryCode
    }

    private fun loadData(country: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getHomeDataUseCase(country).collect { (newsResult, cryptoResult) ->
                val isLoading =
                    newsResult is Result.Loading || cryptoResult is Result.Loading
                val articles = (newsResult as? Result.Success)?.data.orEmpty()
                val cryptos = (cryptoResult as? Result.Success)?.data.orEmpty().take(5)
                val error = when {
                    newsResult is Result.Error -> newsResult.message
                    cryptoResult is Result.Error -> cryptoResult.message
                    else -> null
                }
                _uiState.value = HomeUiState(
                    isLoading = isLoading,
                    articles = articles,
                    cryptos = cryptos,
                    error = error
                )
            }
        }
    }
}
