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
    val localOnly: Boolean = false,
    val locality: String? = null,
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
    private val _localOnly = MutableStateFlow(false)
    private val _locality = MutableStateFlow<String?>(null)

    // Incrementato per forzare un nuovo tentativo di caricamento (pulsante "Riprova")
    private val _refreshTick = MutableStateFlow(0)

    private data class NewsQuery(
        val query: String,
        val category: String,
        val localOnly: Boolean,
        val locality: String?
    )

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
        _selectedCategory,
        _localOnly,
        _locality,
        _refreshTick
    ) { query, category, localOnly, locality, _ ->
        NewsQuery(query, category, localOnly, locality)
    }
        .flatMapLatest { params ->
            val localActive = params.localOnly && !params.locality.isNullOrBlank()
            val flow = when {
                // Ricerca esplicita dell'utente: ha priorità
                params.query.isNotBlank() -> searchNewsUseCase(query = params.query)
                // Notizie locali: cerca per nome della località rilevata
                localActive -> searchNewsUseCase(query = params.locality!!)
                // Default: top headlines per categoria, nessun filtro geografico
                else -> getNewsByCategoryUseCase(category = params.category, country = null)
            }
            flow.map { result ->
                val base = NewsUiState(
                    selectedCategory = params.category,
                    searchQuery = params.query,
                    localOnly = params.localOnly,
                    locality = params.locality
                )
                when (result) {
                    is Result.Loading -> base.copy(isLoading = true)
                    is Result.Success -> base.copy(articles = result.data)
                    is Result.Error -> base.copy(error = result.message)
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

    /** Registra la località rilevata dalla posizione (es. "Bologna"). */
    fun setLocality(name: String) {
        _locality.value = name
    }

    /** Attiva/disattiva il filtro per notizie locali. */
    fun setLocalNews(enabled: Boolean) {
        _localOnly.value = enabled
    }

    fun onSearchQueryChanged(query: String) {
        _rawQuery.value = query
    }

    /** Riprova il caricamento dopo un errore. */
    fun retry() {
        _refreshTick.value++
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
