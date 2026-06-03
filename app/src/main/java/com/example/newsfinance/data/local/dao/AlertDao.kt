package com.example.newsfinance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.newsfinance.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room per gli alert di prezzo. Supporta più alert per crypto.
 */
@Dao
interface AlertDao {

    @Insert
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("DELETE FROM crypto_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("UPDATE crypto_alerts SET triggered = :triggered WHERE id = :id")
    suspend fun updateTriggered(id: Long, triggered: Boolean)

    @Query("SELECT * FROM crypto_alerts WHERE cryptoId = :cryptoId ORDER BY above DESC, threshold ASC")
    fun getAlertsForCrypto(cryptoId: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM crypto_alerts")
    suspend fun getAllAlerts(): List<AlertEntity>
}
