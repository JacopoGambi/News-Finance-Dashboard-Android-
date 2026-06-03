package com.example.newsfinance.data.repository

import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.dto.toDomain
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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

    override suspend fun getCryptoDetail(
        id: String,
        vsCurrency: String
    ): Result<Crypto> = withContext(Dispatchers.IO) {
        try {
            val crypto = coinGeckoService.getMarketsByIds(vsCurrency = vsCurrency, ids = id)
                .firstOrNull()
                ?.toDomain()
            if (crypto != null) {
                Result.Success(crypto)
            } else {
                Result.Error("Crypto non trovata")
            }
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Errore nel recupero del dettaglio", t)
        }
    }

    override suspend fun getPriceChart(
        id: String,
        vsCurrency: String,
        days: Int
    ): Result<List<Pair<Long, Double>>> = withContext(Dispatchers.IO) {
        try {
            val points = coinGeckoService.getMarketChart(
                id = id,
                vsCurrency = vsCurrency,
                days = days
            ).prices.mapNotNull { entry ->
                if (entry.size >= 2) entry[0].toLong() to entry[1] else null
            }
            Result.Success(points)
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Errore nel caricamento del grafico", t)
        }
    }

    override suspend fun getPrices(
        ids: List<String>,
        vsCurrency: String
    ): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext Result.Success(emptyMap())
        try {
            val map = coinGeckoService.getMarketsByIds(
                vsCurrency = vsCurrency,
                ids = ids.distinct().joinToString(",")
            ).associate { dto -> (dto.id ?: "") to (dto.currentPrice ?: 0.0) }
            Result.Success(map)
        } catch (t: Throwable) {
            Result.Error(t.message ?: "Errore nel recupero dei prezzi", t)
        }
    }

    private companion object {
        const val MARKETS_PER_PAGE = 50
        const val CACHE_TTL_MS = 60_000L
    }
}
