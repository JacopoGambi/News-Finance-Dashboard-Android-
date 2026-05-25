package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.domain.repository.FavoritesRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCryptoMarketsUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(vsCurrency: String): Flow<Result<List<Crypto>>> =
        combine(
            cryptoRepository.getMarkets(vsCurrency),
            favoritesRepository.getWatchlistCryptos()
        ) { marketsResult, watchlist ->
            when (marketsResult) {
                is Result.Loading -> Result.Loading
                is Result.Error -> marketsResult
                is Result.Success -> {
                    // Arricchisce i dati remoti con alertThreshold salvati in Room
                    val watchlistMap = watchlist.associateBy { it.id }
                    Result.Success(marketsResult.data.map { crypto ->
                        watchlistMap[crypto.id]?.let { saved ->
                            crypto.copy(alertThreshold = saved.alertThreshold)
                        } ?: crypto
                    })
                }
            }
        }
}
