package com.example.newsfinance.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favoriteArticles: List<Article> = emptyList(),
    val watchlistCryptos: List<Crypto> = emptyList()
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoritesRepository.getFavoriteArticles(),
        favoritesRepository.getWatchlistCryptos()
    ) { articles, cryptos ->
        FavoritesUiState(favoriteArticles = articles, watchlistCryptos = cryptos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState()
    )

    fun onRemoveArticle(article: Article) {
        viewModelScope.launch { favoritesRepository.removeFavoriteArticle(article) }
    }

    fun onRemoveCrypto(crypto: Crypto) {
        viewModelScope.launch { favoritesRepository.removeFromWatchlist(crypto) }
    }
}
