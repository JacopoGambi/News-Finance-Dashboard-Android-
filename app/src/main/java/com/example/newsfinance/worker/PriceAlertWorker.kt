package com.example.newsfinance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.domain.repository.CryptoRepository
import com.example.newsfinance.util.NotificationHelper
import com.example.newsfinance.util.Result as DataResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class PriceAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cryptoRepository: CryptoRepository,
    private val alertRepository: AlertRepository,
    private val notificationHelper: NotificationHelper,
    private val prefsStore: UserPreferencesDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = prefsStore.preferences.first()
            // Se le notifiche sono disabilitate il worker non invia nulla
            if (!prefs.notificationsEnabled) return@withContext Result.success()

            val alerts = alertRepository.getAllAlerts()
            if (alerts.isEmpty()) return@withContext Result.success()

            val currency = prefs.preferredCurrency
            val pricesResult = cryptoRepository.getPrices(
                ids = alerts.map { it.cryptoId },
                vsCurrency = currency
            )
            val priceMap = when (pricesResult) {
                is DataResult.Success -> pricesResult.data
                is DataResult.Error ->
                    return@withContext if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
                DataResult.Loading -> emptyMap()
            }

            alerts.forEach { alert ->
                val currentPrice = priceMap[alert.cryptoId] ?: return@forEach
                val crossed = if (alert.above) {
                    currentPrice >= alert.threshold
                } else {
                    currentPrice <= alert.threshold
                }

                when {
                    // Soglia superata e non ancora notificata: notifica una sola volta
                    crossed && !alert.triggered -> {
                        notificationHelper.sendPriceAlert(
                            notificationId = alert.id.toInt(),
                            cryptoName = alert.cryptoName,
                            currentPrice = currentPrice,
                            threshold = alert.threshold,
                            currency = currency,
                            above = alert.above
                        )
                        alertRepository.setTriggered(alert.id, true)
                    }
                    // Condizione rientrata: riarma l'alert per la prossima volta
                    !crossed && alert.triggered -> {
                        alertRepository.setTriggered(alert.id, false)
                    }
                }
            }

            Result.success()
        } catch (_: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
        }
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
