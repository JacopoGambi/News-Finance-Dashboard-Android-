package com.example.newsfinance.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.newsfinance.ui.favorites.FavoritesScreen
import com.example.newsfinance.ui.home.HomeScreen
import com.example.newsfinance.ui.markets.CryptoDetailScreen
import com.example.newsfinance.ui.markets.MarketsScreen
import com.example.newsfinance.ui.news.NewsScreen
import com.example.newsfinance.ui.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(modifier = Modifier.fillMaxSize())
        }
        composable(Screen.News.route) {
            NewsScreen(modifier = Modifier.fillMaxSize())
        }
        composable(Screen.Markets.route) {
            MarketsScreen(
                modifier = Modifier.fillMaxSize(),
                onCryptoClick = { cryptoId, currency ->
                    navController.navigate(Screen.CryptoDetail.createRoute(cryptoId, currency))
                }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(modifier = Modifier.fillMaxSize())
        }
        composable(Screen.Settings.route) {
            SettingsScreen(modifier = Modifier.fillMaxSize())
        }
        composable(
            route = Screen.CryptoDetail.route,
            arguments = listOf(
                navArgument(Screen.CryptoDetail.ARG_ID) { type = NavType.StringType },
                navArgument(Screen.CryptoDetail.ARG_CURRENCY) { type = NavType.StringType }
            )
        ) {
            CryptoDetailScreen(
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
