package com.example.newsfinance.data.repository

import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.dto.toDomain
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoRepositoryImpl @Inject constructor(
    private val coinGeckoService: CoinGeckoService
) : CryptoRepository {

    private data class CacheEntry(val data: List<Crypto>, val timestamp: Long)

    private val mutex = Mutex()
    // Cache per-valuta: ogni valuta mantiene il proprio ultimo dato, così il
    // fallback su errore (es. 429) funziona anche se le schermate usano valute diverse.
    private val cache = mutableMapOf<String, CacheEntry>()

    override fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>> = flow {
        emit(Result.Loading)

        val now = System.currentTimeMillis()
        val fresh = mutex.withLock {
            cache[vsCurrency]?.takeIf { it.data.isNotEmpty() && now - it.timestamp < CACHE_TTL_MS }?.data
        }
        if (fresh != null) {
            emit(Result.Success(fresh))
            return@flow
        }

        try {
            val response = coinGeckoService.getMarkets(
                vsCurrency = vsCurrency,
                perPage = MARKETS_PER_PAGE,
                page = 1
            )
            val cryptos = response.mapNotNull { it.toDomain() }
            mutex.withLock { cache[vsCurrency] = CacheEntry(cryptos, System.currentTimeMillis()) }
            emit(Result.Success(cryptos))
        } catch (t: Throwable) {
            val fallback = mutex.withLock { cache[vsCurrency]?.data?.takeIf { it.isNotEmpty() } }
            if (fallback != null) {
                emit(Result.Success(fallback))
            } else {
                emit(Result.Error(t.message ?: "Errore nel recupero dei mercati", t))
            }
        }
    }

    private companion object {
        const val MARKETS_PER_PAGE = 50
        const val CACHE_TTL_MS = 60_000L
    }
}
