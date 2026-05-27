package com.example.newsfinance

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.util.Constants
import com.example.newsfinance.util.NotificationHelper
import com.example.newsfinance.worker.PriceAlertWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class NewsFinanceApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var prefsStore: UserPreferencesDataStore
    @Inject lateinit var notificationHelper: NotificationHelper

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
        schedulePriceAlertWorker()
    }

    private fun schedulePriceAlertWorker() {
        applicationScope.launch {
            val intervalMinutes = prefsStore.preferences.first().updateIntervalMinutes.toLong()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PriceAlertWorker>(
                repeatInterval = intervalMinutes,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(Constants.WORKER_PRICE_ALERTS_TAG)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                Constants.WORKER_PRICE_ALERTS_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
