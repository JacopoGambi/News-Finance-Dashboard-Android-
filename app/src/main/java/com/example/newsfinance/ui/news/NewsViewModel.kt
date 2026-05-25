package com.example.newsfinance.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.domain.usecase.GetNewsByCategoryUseCase
import com.example.newsfinance.domain.usecase.SearchNewsUseCase
import com.example.newsfinance.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewsUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val selectedCategory: String = "general",
    val searchQuery: String = "",
    val error: String? = null,
    val favoriteIds: Set<String> = emptySet()
)

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsByCategoryUseCase: GetNewsByCategoryUseCase,
    private val searchNewsUseCase: SearchNewsUseCase,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _rawQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("general")

    // Debounce 500ms sulla ricerca, ma svuotamento immediato (es. selezione categoria)
    private val _effectiveQuery = _rawQuery.transformLatest { query ->
        if (query.isBlank()) {
            emit(query)
        } else {
            delay(500)
            emit(query)
        }
    }

    private val _favoriteIds: StateFlow<Set<String>> = favoritesRepository
        .getFavoriteArticles()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val uiState: StateFlow<NewsUiState> = combine(
        _effectiveQuery,
        _selectedCategory
    ) { query, category -> Pair(query, category) }
        .flatMapLatest { (query, category) ->
            val flow = if (query.isBlank()) {
                getNewsByCategoryUseCase(category = category, country = "us")
            } else {
                searchNewsUseCase(query = query)
            }
            flow.map { result ->
                when (result) {
                    is Result.Loading -> NewsUiState(
                        isLoading = true,
                        selectedCategory = category,
                        searchQuery = query
                    )
                    is Result.Success -> NewsUiState(
                        articles = result.data,
                        selectedCategory = category,
                        searchQuery = query
                    )
                    is Result.Error -> NewsUiState(
                        error = result.message,
                        selectedCategory = category,
                        searchQuery = query
                    )
                }
            }
        }
        // Aggiorna i favoriti senza toccare il resto dello stato
        .combine(_favoriteIds) { state, favIds ->
            state.copy(favoriteIds = favIds)
        }
        // searchQuery riflette l'input grezzo (senza ritardo nel TextField)
        .combine(_rawQuery) { state, rawQuery ->
            state.copy(searchQuery = rawQuery)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsUiState(isLoading = true)
        )

    fun onCategorySelected(category: String) {
        _rawQuery.value = ""           // svuota immediatamente la ricerca
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _rawQuery.value = query
    }

    fun onToggleFavorite(article: Article) {
        viewModelScope.launch {
            if (article.id in _favoriteIds.value) {
                favoritesRepository.removeFavoriteArticle(article)
            } else {
                favoritesRepository.addFavoriteArticle(article)
            }
        }
    }
}
