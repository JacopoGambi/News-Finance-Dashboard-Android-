package com.example.newsfinance.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object News : Screen("news", "News", Icons.Filled.Newspaper)
    data object Markets : Screen("markets", "Mercati", Icons.Filled.ShowChart)
    data object Favorites : Screen("favorites", "Preferiti", Icons.Filled.Favorite)
    data object Settings : Screen("settings", "Impostazioni", Icons.Filled.Settings)

    data object CryptoDetail : Screen(
        route = "crypto_detail/{cryptoId}/{currency}",
        label = "Dettaglio",
        icon = Icons.Filled.ShowChart
    ) {
        fun createRoute(cryptoId: String, currency: String) =
            "crypto_detail/${Uri.encode(cryptoId)}/$currency"
        const val ARG_ID = "cryptoId"
        const val ARG_CURRENCY = "currency"
    }
}
