package com.example.newsfinance

import com.example.newsfinance.data.local.UserPreferences
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.data.remote.dto.NewsResponseDto
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.model.CryptoAlert
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

/** Preferenze in memoria, senza DataStore/Context. */
class FakeUserPreferencesDataStore(
    initial: UserPreferences = UserPreferences()
) : UserPreferencesDataStore {
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state

    override suspend fun updateCurrency(currency: String) =
        state.update { it.copy(preferredCurrency = currency) }
    override suspend fun updateNotificationsEnabled(enabled: Boolean) =
        state.update { it.copy(notificationsEnabled = enabled) }
    override suspend fun updateIntervalMinutes(minutes: Int) =
        state.update { it.copy(updateIntervalMinutes = minutes) }
    override suspend fun updateLang(lang: String) =
        state.update { it.copy(preferredLang = lang) }
}

/** Servizio notizie che registra le chiamate e restituisce risposte configurabili. */
class FakeNewsApiService : NewsApiService {
    data class TopCall(val category: String?, val lang: String, val country: String?)
    data class SearchCall(val query: String, val lang: String, val country: String?)

    val topCalls = mutableListOf<TopCall>()
    val searchCalls = mutableListOf<SearchCall>()

    var topResponse: (country: String?) -> NewsResponseDto = { NewsResponseDto(0, emptyList()) }
    var searchResponse: NewsResponseDto = NewsResponseDto(0, emptyList())

    override suspend fun getTopHeadlines(
        category: String?,
        lang: String,
        country: String?,
        max: Int,
        apiKey: String
    ): NewsResponseDto {
        topCalls.add(TopCall(category, lang, country))
        return topResponse(country)
    }

    override suspend fun searchNews(
        query: String,
        lang: String,
        country: String?,
        max: Int,
        apiKey: String
    ): NewsResponseDto {
        searchCalls.add(SearchCall(query, lang, country))
        return searchResponse
    }
}

/** Repository crypto con risultati configurabili. */
class FakeCryptoRepository(
    private var marketsResult: Result<List<Crypto>> = Result.Success(emptyList())
) : CryptoRepository {
    override fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>> = flowOf(marketsResult)
    override suspend fun getCryptoDetail(id: String, vsCurrency: String): Result<Crypto> =
        Result.Error("not used")
    override suspend fun getPriceChart(id: String, vsCurrency: String, days: Int): Result<List<Pair<Long, Double>>> =
        Result.Success(emptyList())
    override suspend fun getPrices(ids: List<String>, vsCurrency: String): Result<Map<String, Double>> =
        Result.Success(emptyMap())
}

/** Favorites repository no-op per i test dei ViewModel. */
class FakeFavoritesRepository : FavoritesRepository {
    override fun getFavoriteArticles(): Flow<List<Article>> = flowOf(emptyList())
    override suspend fun addFavoriteArticle(article: Article) {}
    override suspend fun removeFavoriteArticle(article: Article) {}
    override fun isArticleFavorite(url: String): Flow<Boolean> = flowOf(false)
    override fun getWatchlistCryptos(): Flow<List<Crypto>> = flowOf(emptyList())
    override suspend fun addToWatchlist(crypto: Crypto) {}
    override suspend fun removeFromWatchlist(crypto: Crypto) {}
}

/** Alert repository no-op per i test dei ViewModel. */
class FakeAlertRepository : AlertRepository {
    override fun getAlertsForCrypto(cryptoId: String): Flow<List<CryptoAlert>> = flowOf(emptyList())
    override suspend fun addAlert(cryptoId: String, cryptoName: String, threshold: Double, above: Boolean) {}
    override suspend fun removeAlert(id: Long) {}
    override suspend fun getAllAlerts(): List<CryptoAlert> = emptyList()
    override suspend fun setTriggered(id: Long, triggered: Boolean) {}
}
