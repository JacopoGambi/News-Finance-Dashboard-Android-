package com.example.newsfinance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.domain.repository.AlertRepository
import com.example.newsfinance.util.Constants
import com.example.newsfinance.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class PriceAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val coinGeckoService: CoinGeckoService,
    private val alertRepository: AlertRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val alerts = alertRepository.getAllAlerts()
            if (alerts.isEmpty()) return@withContext Result.success()

            val ids = alerts.map { it.cryptoId }.distinct().joinToString(",")
            val fetchedPrices = coinGeckoService.getMarketsByIds(
                vsCurrency = Constants.DEFAULT_VS_CURRENCY,
                ids = ids
            )

            val priceMap = fetchedPrices.associate { dto ->
                (dto.id ?: "") to (dto.currentPrice ?: 0.0)
            }

            alerts.forEach { alert ->
                val currentPrice = priceMap[alert.cryptoId] ?: return@forEach
                val crossed = if (alert.above) {
                    currentPrice >= alert.threshold
                } else {
                    currentPrice <= alert.threshold
                }
                if (crossed) {
                    notificationHelper.sendPriceAlert(
                        notificationId = alert.id.toInt(),
                        cryptoName = alert.cryptoName,
                        currentPrice = currentPrice,
                        threshold = alert.threshold,
                        above = alert.above
                    )
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
