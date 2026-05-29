package com.example.newsfinance.ui.markets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.domain.usecase.GetCryptoMarketsUseCase
import com.example.newsfinance.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketsUiState(
    val isLoading: Boolean = false,
    val cryptos: List<Crypto> = emptyList(),
    val selectedCurrency: String = "usd",
    val error: String? = null,
    val watchlistIds: Set<String> = emptySet()
)

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val getMarketsUseCase: GetCryptoMarketsUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val alertRepository: AlertRepository,
    private val prefsStore: UserPreferencesDataStore
) : ViewModel() {

    private val _selectedCurrency = MutableStateFlow("usd")

    init {
        viewModelScope.launch {
            val prefs = prefsStore.preferences
            prefs.collect { _selectedCurrency.value = it.preferredCurrency }
        }
    }
    // Incrementato a ogni refresh per forzare il ricaricamento con la stessa valuta
    private val _refreshTick = MutableStateFlow(0)

    private val _watchlistIds: StateFlow<Set<String>> = favoritesRepository
        .getWatchlistCryptos()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val uiState: StateFlow<MarketsUiState> = combine(
        _selectedCurrency,
        _refreshTick
    ) { currency, _ -> currency }
        .flatMapLatest { currency ->
            getMarketsUseCase(vsCurrency = currency).map { result ->
                when (result) {
                    is Result.Loading -> MarketsUiState(
                        isLoading = true,
                        selectedCurrency = currency
                    )
                    is Result.Success -> MarketsUiState(
                        cryptos = result.data,
                        selectedCurrency = currency
                    )
                    is Result.Error -> MarketsUiState(
                        error = result.message,
                        selectedCurrency = currency
                    )
                }
            }
        }
        .combine(_watchlistIds) { state, ids ->
            state.copy(watchlistIds = ids)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MarketsUiState(isLoading = true)
        )

    fun onCurrencyChanged(currency: String) {
        _selectedCurrency.value = currency
    }

    fun refresh() {
        _refreshTick.value++
    }

    fun onToggleWatchlist(crypto: Crypto) {
        viewModelScope.launch {
            if (crypto.id in _watchlistIds.value) {
                favoritesRepository.removeFromWatchlist(crypto)
            } else {
                favoritesRepository.addToWatchlist(crypto)
            }
        }
    }

    fun onAddAlert(crypto: Crypto, threshold: Double, above: Boolean) {
        viewModelScope.launch {
            alertRepository.addAlert(crypto.id, crypto.name, threshold, above)
        }
    }
}
