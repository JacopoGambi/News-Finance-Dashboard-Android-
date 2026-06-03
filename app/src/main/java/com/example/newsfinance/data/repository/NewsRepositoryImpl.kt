package com.example.newsfinance.data.repository

import com.example.newsfinance.data.remote.api.NewsApiService
import com.example.newsfinance.data.remote.dto.toDomain
import com.example.newsfinance.domain.model.Article
import com.example.newsfinance.domain.repository.NewsRepository
import com.example.newsfinance.util.Constants
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val newsApiService: NewsApiService
) : NewsRepository {

    private data class CacheEntry(val data: List<Article>, val timestamp: Long)

    private val mutex = Mutex()
    private val cache = mutableMapOf<String, CacheEntry>()

    override fun getTopHeadlines(
        country: String?,
        category: String?
    ): Flow<Result<List<Article>>> = flow {
        val lang = currentLang()
        val key = "headlines:$lang:${country.orEmpty()}:${category.orEmpty()}"
        loadWithCache(key, "Errore nel recupero delle notizie") {
            val response = newsApiService.getTopHeadlines(
                category = category,
                lang = lang,
                country = country,
                max = Constants.DEFAULT_NEWS_MAX,
                apiKey = Constants.GNEWS_API_KEY
            )
            response.articles.orEmpty().mapNotNull { it.toDomain(category) }
        }
    }

    override fun searchNews(query: String): Flow<Result<List<Article>>> = flow {
        val lang = currentLang()
        loadWithCache("search:$lang:$query", "Errore nella ricerca") {
            val response = newsApiService.searchNews(
                query = query,
                lang = lang,
                country = Constants.DEFAULT_COUNTRY,
                max = Constants.DEFAULT_NEWS_MAX,
                apiKey = Constants.GNEWS_API_KEY
            )
            response.articles.orEmpty().mapNotNull { it.toDomain() }
        }
    }

    /**
     * Lingua corrente dell'app (segue il locale scelto in Opzioni) limitata a quelle
     * supportate da GNews; fallback su inglese per locale non gestiti.
     */
    private fun currentLang(): String {
        val lang = Locale.getDefault().language
        return if (lang in SUPPORTED_LANGS) lang else "en"
    }

    /**
     * Esegue [fetch] gestendo cache e fallback:
     * - se la cache per [key] è ancora valida (entro il TTL) la restituisce senza chiamare la rete;
     * - in caso di errore di rete (es. HTTP 429 rate limit) riemette l'ultimo dato caricato per [key],
     *   così l'utente continua a vedere i contenuti finché la rete non torna disponibile;
     * - se non c'è alcun dato in cache, emette l'errore.
     */
    private suspend fun FlowCollector<Result<List<Article>>>.loadWithCache(
        key: String,
        errorMessage: String,
        fetch: suspend () -> List<Article>
    ) {
        emit(Result.Loading)

        val now = System.currentTimeMillis()
        val fresh = mutex.withLock {
            cache[key]?.takeIf { it.data.isNotEmpty() && now - it.timestamp < CACHE_TTL_MS }?.data
        }
        if (fresh != null) {
            emit(Result.Success(fresh))
            return
        }

        try {
            val data = fetch()
            mutex.withLock { cache[key] = CacheEntry(data, System.currentTimeMillis()) }
            emit(Result.Success(data))
        } catch (t: Throwable) {
            val fallback = mutex.withLock { cache[key]?.data?.takeIf { it.isNotEmpty() } }
            if (fallback != null) {
                emit(Result.Success(fallback))
            } else {
                emit(Result.Error(t.message ?: errorMessage, t))
            }
        }
    }

    private companion object {
        const val CACHE_TTL_MS = 60_000L
        val SUPPORTED_LANGS = setOf("it", "en", "es", "fr")
    }
}
