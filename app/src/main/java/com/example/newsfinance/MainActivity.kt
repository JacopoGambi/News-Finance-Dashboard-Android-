package com.example.newsfinance

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.newsfinance.ui.navigation.NavGraph
import com.example.newsfinance.ui.navigation.Screen
import com.example.newsfinance.ui.theme.NewsFinanceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFinanceTheme {
                val navController = rememberNavController()
                val tabs = listOf(
                    Screen.Home,
                    Screen.News,
                    Screen.Markets,
                    Screen.Favorites,
                    Screen.Settings
                )
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        NavigationBar {
                            tabs.forEach { screen ->
                                val label = stringResource(screen.labelRes)
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    selected = currentDestination?.hierarchy
                                        ?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
