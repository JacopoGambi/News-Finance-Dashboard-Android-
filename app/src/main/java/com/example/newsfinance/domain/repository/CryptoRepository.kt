package com.example.newsfinance.domain.repository

import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.util.Result
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {

    fun getMarkets(vsCurrency: String): Flow<Result<List<Crypto>>>

    /** Dati di dettaglio (nome, simbolo, prezzo, variazione) di una singola crypto. */
    suspend fun getCryptoDetail(id: String, vsCurrency: String): Result<Crypto>

    /** Serie storica dei prezzi (timestamp, prezzo) per il grafico. */
    suspend fun getPriceChart(id: String, vsCurrency: String, days: Int): Result<List<Pair<Long, Double>>>

    /** Prezzi correnti per un insieme di crypto, mappati per id (usato dal worker degli alert). */
    suspend fun getPrices(ids: List<String>, vsCurrency: String): Result<Map<String, Double>>
}
