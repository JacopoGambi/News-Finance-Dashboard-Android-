package com.example.newsfinance.data.repository

import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.data.remote.dto.toDomain
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val coinGeckoService: CoinGeckoService
) : CryptoRepository {

    override fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>> = flow {
        emit(Result.Loading)
        try {
            val response = coinGeckoService.getMarkets(
                vsCurrency = vsCurrency,
                perPage = MARKETS_PER_PAGE,
                page = 1
            )
            val cryptos = response.mapNotNull { it.toDomain() }
            emit(Result.Success(cryptos))
        } catch (t: Throwable) {
            emit(Result.Error(t.message ?: "Errore nel recupero dei mercati", t))
        }
    }

    private companion object {
        const val MARKETS_PER_PAGE = 50
    }
}
