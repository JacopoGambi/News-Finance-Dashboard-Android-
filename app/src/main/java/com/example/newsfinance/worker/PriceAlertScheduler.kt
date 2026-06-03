package com.example.newsfinance.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.newsfinance.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pianifica e annulla il worker periodico degli alert di prezzo.
 * Centralizzato così Application (avvio) e Impostazioni (cambi runtime)
 * usano la stessa logica di scheduling.
 */
@Singleton
class PriceAlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * (Ri)pianifica il worker con l'intervallo dato. Usa la policy UPDATE così
     * un cambio di intervallo sostituisce la pianificazione esistente.
     */
    fun schedule(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(Constants.WORKER_PRICE_ALERTS_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.WORKER_PRICE_ALERTS_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** Annulla il worker (es. quando le notifiche vengono disabilitate). */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(Constants.WORKER_PRICE_ALERTS_TAG)
    }
}
