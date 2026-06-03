package com.example.newsfinance.ui.markets

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.R
import com.example.newsfinance.domain.model.CryptoAlert
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.ui.navigation.Screen
import com.example.newsfinance.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChartTimeRange(@StringRes val labelRes: Int, val days: Int) {
    ONE_DAY(R.string.range_1d, 1),
    ONE_WEEK(R.string.range_1w, 7),
    ONE_MONTH(R.string.range_1m, 30),
    THREE_MONTHS(R.string.range_3m, 90),
    ONE_YEAR(R.string.range_1y, 365)
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
    val currency: String = "usd",
    val alerts: List<CryptoAlert> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CryptoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cryptoRepository: CryptoRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val cryptoId: String = checkNotNull(savedStateHandle[Screen.CryptoDetail.ARG_ID])
    private val currency: String = checkNotNull(savedStateHandle[Screen.CryptoDetail.ARG_CURRENCY])

    private val _uiState = MutableStateFlow(CryptoDetailUiState(currency = currency))
    val uiState: StateFlow<CryptoDetailUiState> = _uiState.asStateFlow()

    init {
        loadHeaderInfo()
        loadChart(ChartTimeRange.ONE_WEEK)
        observeAlerts()
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            alertRepository.getAlertsForCrypto(cryptoId).collect { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
    }

    fun onRemoveAlert(id: Long) {
        viewModelScope.launch { alertRepository.removeAlert(id) }
    }

    fun onRangeSelected(range: ChartTimeRange) {
        if (range == _uiState.value.selectedRange) return
        _uiState.update { it.copy(isLoading = true, selectedRange = range) }
        loadChart(range)
    }

    private fun loadHeaderInfo() {
        viewModelScope.launch {
            when (val result = cryptoRepository.getCryptoDetail(cryptoId, currency)) {
                is Result.Success -> _uiState.update { state ->
                    val crypto = result.data
                    state.copy(
                        cryptoName = crypto.name,
                        cryptoSymbol = crypto.symbol.uppercase(),
                        imageUrl = crypto.imageUrl,
                        currentPrice = crypto.currentPrice,
                        priceChange24h = crypto.priceChangePercentage24h
                    )
                }
                // Fallback silenzioso: l'header mostra solo l'ID
                is Result.Error, Result.Loading -> Unit
            }
        }
    }

    private fun loadChart(range: ChartTimeRange) {
        viewModelScope.launch {
            when (val result = cryptoRepository.getPriceChart(cryptoId, currency, range.days)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(isLoading = false, chartPoints = result.data, error = null)
                }
                is Result.Error -> _uiState.update { state ->
                    state.copy(isLoading = false, error = result.message)
                }
                Result.Loading -> Unit
            }
        }
    }
}
