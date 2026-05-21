package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {

    fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>>
}
