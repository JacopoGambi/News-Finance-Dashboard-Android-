package com.example.newsfinance.domain.usecase

import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCryptoMarketsUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) {
    operator fun invoke(vsCurrency: String): Flow<Result<List<Crypto>>> =
        cryptoRepository.getMarkets(vsCurrency)
}
