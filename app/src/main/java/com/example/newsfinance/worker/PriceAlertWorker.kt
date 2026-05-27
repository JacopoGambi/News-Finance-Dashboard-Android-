package com.example.newsfinance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.newsfinance.data.remote.api.CoinGeckoService
import com.example.newsfinance.domain.repository.FavoritesRepository
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
    private val favoritesRepository: FavoritesRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val cryptosWithAlerts = favoritesRepository.getCryptosWithAlerts()
            if (cryptosWithAlerts.isEmpty()) return@withContext Result.success()

            val ids = cryptosWithAlerts.joinToString(",") { it.id }
            val fetchedPrices = coinGeckoService.getMarketsByIds(
                vsCurrency = Constants.DEFAULT_VS_CURRENCY,
                ids = ids
            )

            val priceMap = fetchedPrices.associate { dto ->
                (dto.id ?: "") to (dto.currentPrice ?: 0.0)
            }

            cryptosWithAlerts.forEach { crypto ->
                val threshold = crypto.alertThreshold ?: return@forEach
                val currentPrice = priceMap[crypto.id] ?: return@forEach
                if (currentPrice >= threshold) {
                    notificationHelper.sendPriceAlert(
                        cryptoName = crypto.name,
                        cryptoSymbol = crypto.symbol,
                        currentPrice = currentPrice,
                        threshold = threshold
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
