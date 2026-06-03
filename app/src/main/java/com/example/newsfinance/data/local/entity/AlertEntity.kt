package com.example.newsfinance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.newsfinance.domain.model.CryptoAlert

/**
 * Entità Room per un alert di prezzo. Una crypto può avere più alert.
 */
@Entity(tableName = "crypto_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cryptoId: String,
    val cryptoName: String,
    val threshold: Double,
    val above: Boolean,
    // True quando la soglia è già stata superata e la notifica inviata:
    // evita notifiche ripetute a ogni ciclo del worker finché la condizione resta vera.
    val triggered: Boolean = false
)

fun AlertEntity.toDomain(): CryptoAlert = CryptoAlert(
    id = id,
    cryptoId = cryptoId,
    cryptoName = cryptoName,
    threshold = threshold,
    above = above,
    triggered = triggered
)

fun CryptoAlert.toEntity(): AlertEntity = AlertEntity(
    id = id,
    cryptoId = cryptoId,
    cryptoName = cryptoName,
    threshold = threshold,
    above = above,
    triggered = triggered
)
