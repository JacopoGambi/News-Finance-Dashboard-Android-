package com.example.newsfinance.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getHomeDataUseCase: GetHomeDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getHomeDataUseCase().collect { (newsResult, cryptoResult) ->
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
