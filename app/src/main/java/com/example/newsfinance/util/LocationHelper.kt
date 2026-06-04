package com.example.newsfinance.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    /** Località rilevata: nome (città/provincia/regione) e codice paese ISO. */
    data class DetectedPlace(val locality: String, val countryCode: String?)

    @SuppressLint("MissingPermission")
    suspend fun getCountryCode(context: Context): String? = withContext(Dispatchers.IO) {
        resolveAddress(context)?.countryCode?.lowercase()
    }

    /**
     * Nome della località corrente (città, provincia o regione) da usare come
     * termine di ricerca per le notizie locali. Null se non determinabile.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLocality(context: Context): String? = withContext(Dispatchers.IO) {
        resolveAddress(context)?.toLocality()
    }

    /**
     * Località e paese in un'unica geocodifica: usato dalle notizie locali per
     * mostrare i contenuti nella lingua del paese rilevato.
     */
    @SuppressLint("MissingPermission")
    suspend fun getDetectedPlace(context: Context): DetectedPlace? = withContext(Dispatchers.IO) {
        val address = resolveAddress(context) ?: return@withContext null
        val locality = address.toLocality() ?: return@withContext null
        DetectedPlace(locality, address.countryCode?.lowercase())
    }

    /** Geocodifica la posizione corrente in un indirizzo, o null se non disponibile. */
    @SuppressLint("MissingPermission")
    private suspend fun resolveAddress(context: Context): Address? {
        if (!Geocoder.isPresent()) return null
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val location = client.awaitLocation() ?: return null
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Posizione corrente: prova prima l'ultima posizione nota (immediata) e in
     * mancanza richiede un nuovo fix. Su emulatori/device senza posizione in
     * cache `lastLocation` è spesso null, quindi il fix attivo è essenziale.
     */
    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.awaitLocation(): Location? {
        lastLocation.await()?.let { return it }
        val cts = CancellationTokenSource()
        return try {
            getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
        } catch (_: Exception) {
            null
        } finally {
            cts.cancel()
        }
    }

    private fun Address.toLocality(): String? =
        locality ?: subAdminArea ?: adminArea
}
