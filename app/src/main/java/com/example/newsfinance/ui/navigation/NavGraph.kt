package com.example.newsfinance.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onCryptoClick = { cryptoId, currency ->
                    navController.navigate(Screen.CryptoDetail.createRoute(cryptoId, currency))
                }
            )
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
            FavoritesScreen(
                modifier = Modifier.fillMaxSize(),
                onCryptoClick = { cryptoId, currency ->
                    navController.navigate(Screen.CryptoDetail.createRoute(cryptoId, currency))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(modifier = Modifier.fillMaxSize())
        }
        composable(
            route = Screen.CryptoDetail.route,
            arguments = listOf(
                navArgument(Screen.CryptoDetail.ARG_ID) { type = NavType.StringType },
                navArgument(Screen.CryptoDetail.ARG_CURRENCY) { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
            },
            exitTransition = { fadeOut(animationSpec = tween(220)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
            }
        ) {
            CryptoDetailScreen(
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
