package com.example.newsfinance.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.newsfinance.ui.home.HomeScreen
import com.example.newsfinance.ui.favorites.FavoritesScreen
import com.example.newsfinance.ui.markets.MarketsScreen
import com.example.newsfinance.ui.news.NewsScreen

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
            MarketsScreen(modifier = Modifier.fillMaxSize())
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(modifier = Modifier.fillMaxSize())
        }
        composable(Screen.Settings.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Impostazioni")
            }
        }
    }
}
