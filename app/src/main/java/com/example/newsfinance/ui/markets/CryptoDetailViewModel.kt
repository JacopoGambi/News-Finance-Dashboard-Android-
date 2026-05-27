package com.example.newsfinance.ui.markets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChartTimeRange(val label: String, val days: Int) {
    ONE_DAY("1G", 1),
    ONE_WEEK("1S", 7),
    ONE_MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    ONE_YEAR("1A", 365)
}

data class CryptoDetailUiState(
    val isLoading: Boolean = true,
    val cryptoName: String = "",
    val cryptoSymbol: String = "",
    val imageUrl: String? = null,
    val currentPrice: Double? = null,
    val priceChange24h: Double? = null,
    val chartPoints: List<Pair<Long, Double>> = emptyList(),
    val selectedRange: ChartTimeRange = ChartTimeRange.ONE_WEEK,
    val error: String? = null
)

@HiltViewModel
class CryptoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coinGeckoService: CoinGeckoService
) : ViewModel() {

    private val cryptoId: String = checkNotNull(savedStateHandle[Screen.CryptoDetail.ARG_ID])
    private val currency: String = checkNotNull(savedStateHandle[Screen.CryptoDetail.ARG_CURRENCY])

    private val _uiState = MutableStateFlow(CryptoDetailUiState())
    val uiState: StateFlow<CryptoDetailUiState> = _uiState.asStateFlow()

    init {
        loadHeaderInfo()
        loadChart(ChartTimeRange.ONE_WEEK)
    }

    fun onRangeSelected(range: ChartTimeRange) {
        if (range == _uiState.value.selectedRange) return
        _uiState.update { it.copy(isLoading = true, selectedRange = range) }
        loadChart(range)
    }

    private fun loadHeaderInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = coinGeckoService.getMarketsByIds(
                    vsCurrency = currency,
                    ids = cryptoId
                )
                list.firstOrNull()?.let { dto ->
                    _uiState.update { state ->
                        state.copy(
                            cryptoName = dto.name ?: cryptoId,
                            cryptoSymbol = dto.symbol?.uppercase() ?: "",
                            imageUrl = dto.image,
                            currentPrice = dto.currentPrice,
                            priceChange24h = dto.priceChangePercentage24h
                        )
                    }
                }
            } catch (_: Exception) { /* fallback silenzioso: l'header mostra solo l'ID */ }
        }
    }

    private fun loadChart(range: ChartTimeRange) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val chart = coinGeckoService.getMarketChart(
                    id = cryptoId,
                    vsCurrency = currency,
                    days = range.days
                )
                val points = chart.prices.mapNotNull { entry ->
                    if (entry.size >= 2) entry[0].toLong() to entry[1] else null
                }
                _uiState.update { state ->
                    state.copy(isLoading = false, chartPoints = points, error = null)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Errore nel caricamento del grafico"
                    )
                }
            }
        }
    }
}
