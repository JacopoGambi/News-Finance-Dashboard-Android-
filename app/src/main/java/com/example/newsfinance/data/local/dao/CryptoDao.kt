package com.example.newsfinance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsfinance.data.local.entity.CryptoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room per le crypto in watchlist e relative soglie di alert.
 */
@Dao
interface CryptoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrypto(crypto: CryptoEntity)

    @Delete
    suspend fun deleteCrypto(crypto: CryptoEntity)

    @Query("SELECT * FROM cryptos ORDER BY marketCapRank ASC")
    fun getAllWatchlistCryptos(): Flow<List<CryptoEntity>>

    @Query("SELECT * FROM cryptos WHERE id = :id")
    suspend fun getCryptoById(id: String): CryptoEntity?

    @Query("UPDATE cryptos SET alertThreshold = :threshold WHERE id = :id")
    suspend fun updateAlertThreshold(id: String, threshold: Double)

    @Query("SELECT * FROM cryptos WHERE alertThreshold IS NOT NULL")
    suspend fun getCryptosWithAlerts(): List<CryptoEntity>
}
