package com.example.newsfinance.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    /** Località rilevata: nome (città/provincia/regione) e codice paese ISO. */
    data class DetectedPlace(val locality: String, val countryCode: String?)

    @SuppressLint("MissingPermission")
    suspend fun getCountryCode(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.lastLocation.await() ?: return@withContext null
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.countryCode?.lowercase()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Nome della località corrente (città, provincia o regione) da usare come
     * termine di ricerca per le notizie locali. Null se non determinabile.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLocality(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.lastLocation.await() ?: return@withContext null
            @Suppress("DEPRECATION")
            val address = Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull() ?: return@withContext null
            address.locality
                ?: address.subAdminArea
                ?: address.adminArea
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Località e paese in un'unica geocodifica: usato dalle notizie locali per
     * mostrare i contenuti nella lingua del paese rilevato.
     */
    @SuppressLint("MissingPermission")
    suspend fun getDetectedPlace(context: Context): DetectedPlace? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.lastLocation.await() ?: return@withContext null
            @Suppress("DEPRECATION")
            val address = Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull() ?: return@withContext null
            val locality = address.locality
                ?: address.subAdminArea
                ?: address.adminArea
                ?: return@withContext null
            DetectedPlace(locality, address.countryCode?.lowercase())
        } catch (_: Exception) {
            null
        }
    }
}
