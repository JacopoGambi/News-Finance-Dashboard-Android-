package com.example.newsfinance

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.newsfinance.data.local.UserPreferencesDataStore
import com.example.newsfinance.util.NotificationHelper
import com.example.newsfinance.worker.PriceAlertScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NewsFinanceApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var prefsStore: UserPreferencesDataStore
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var priceAlertScheduler: PriceAlertScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()
        syncPriceAlertWorker()
    }

    /** Allinea il worker alle preferenze correnti: pianifica se le notifiche sono attive, altrimenti annulla. */
    private fun syncPriceAlertWorker() {
        applicationScope.launch {
            val prefs = prefsStore.preferences.first()
            if (prefs.notificationsEnabled) {
                priceAlertScheduler.schedule(prefs.updateIntervalMinutes.toLong())
            } else {
                priceAlertScheduler.cancel()
            }
        }
    }
}
