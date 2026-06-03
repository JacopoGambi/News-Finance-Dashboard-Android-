package com.example.newsfinance.ui.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.newsfinance.R

sealed class Screen(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    data object News : Screen("news", R.string.nav_news, Icons.Filled.Newspaper)
    data object Markets : Screen("markets", R.string.nav_markets, Icons.Filled.ShowChart)
    data object Favorites : Screen("favorites", R.string.nav_favorites, Icons.Filled.Favorite)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)

    data object CryptoDetail : Screen(
        route = "crypto_detail/{cryptoId}/{currency}",
        labelRes = R.string.nav_detail,
        icon = Icons.Filled.ShowChart
    ) {
        fun createRoute(cryptoId: String, currency: String) =
            "crypto_detail/${Uri.encode(cryptoId)}/$currency"
        const val ARG_ID = "cryptoId"
        const val ARG_CURRENCY = "currency"
    }
}
