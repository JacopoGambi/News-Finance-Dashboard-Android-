package com.example.newsfinance.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_PRICE_ALERTS,
                "Avvisi Prezzo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifiche quando una crypto supera la soglia impostata"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun sendPriceAlert(
        cryptoName: String,
        cryptoSymbol: String,
        currentPrice: Double,
        threshold: Double
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(
            context,
            Constants.NOTIFICATION_CHANNEL_PRICE_ALERTS
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$cryptoName ($cryptoSymbol) ha superato la soglia")
            .setContentText(
                "Prezzo: ${"%.2f".format(currentPrice)} — Soglia: ${"%.2f".format(threshold)}"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(cryptoName.hashCode(), notification)
    }
}
