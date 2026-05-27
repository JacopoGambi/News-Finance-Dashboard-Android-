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

class CryptoRepositoryImpl @Inject constructor(
    private val coinGeckoService: CoinGeckoService
) : CryptoRepository {

    private val mutex = Mutex()
    private var cachedCurrency: String? = null
    private var cachedData: List<Crypto> = emptyList()
    private var cacheTimestamp: Long = 0L

    override fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>> = flow {
        emit(Result.Loading)

        val now = System.currentTimeMillis()
        val cached = mutex.withLock {
            if (cachedCurrency == vsCurrency && cachedData.isNotEmpty()
                && now - cacheTimestamp < CACHE_TTL_MS
            ) cachedData else null
        }

        if (cached != null) {
            emit(Result.Success(cached))
            return@flow
        }

        try {
            val response = coinGeckoService.getMarkets(
                vsCurrency = vsCurrency,
                perPage = MARKETS_PER_PAGE,
                page = 1
            )
            val cryptos = response.mapNotNull { it.toDomain() }
            mutex.withLock {
                cachedCurrency = vsCurrency
                cachedData = cryptos
                cacheTimestamp = System.currentTimeMillis()
            }
            emit(Result.Success(cryptos))
        } catch (t: Throwable) {
            val fallback = mutex.withLock {
                if (cachedCurrency == vsCurrency && cachedData.isNotEmpty()) cachedData else null
            }
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
