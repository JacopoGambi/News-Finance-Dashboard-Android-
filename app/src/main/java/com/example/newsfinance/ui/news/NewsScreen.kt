package com.example.newsfinance.ui.news

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.R
import com.example.newsfinance.ui.components.ArticleCard
import com.example.newsfinance.ui.components.EmptyState
import com.example.newsfinance.ui.components.ErrorState
import com.example.newsfinance.ui.components.LoadingState
import com.example.newsfinance.util.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

private val CATEGORIES = listOf(
    "general" to R.string.category_general,
    "business" to R.string.category_business,
    "technology" to R.string.category_technology,
    "sports" to R.string.category_sports,
    "entertainment" to R.string.category_entertainment,
    "health" to R.string.category_health,
    "science" to R.string.category_science
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    // L'utente vuole attivare il filtro locale: in attesa del permesso/posizione
    var wantLocalNews by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    // Quando il permesso è concesso e l'utente ha richiesto le notizie locali,
    // rileva il paese e attiva il filtro.
    LaunchedEffect(wantLocalNews, locationPermissionState.status) {
        if (wantLocalNews && locationPermissionState.status.isGranted) {
            val place = LocationHelper.getDetectedPlace(context)
            if (place != null) {
                viewModel.setLocality(place.locality, place.countryCode)
                viewModel.setLocalNews(true)
            } else {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.news_location_unavailable)
                )
                wantLocalNews = false
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_news)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Barra di ricerca
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.news_search_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.news_clear_search)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // Filtri (visibili solo quando non si sta cercando)
            if (uiState.searchQuery.isEmpty()) {
                // Toggle notizie locali: di default le notizie non sono filtrate per posizione
                FilterChip(
                    selected = uiState.localOnly,
                    onClick = {
                        if (uiState.localOnly) {
                            // Disattiva il filtro: torna a tutte le notizie
                            viewModel.setLocalNews(false)
                            wantLocalNews = false
                        } else {
                            wantLocalNews = true
                            if (!locationPermissionState.status.isGranted) {
                                locationPermissionState.launchPermissionRequest()
                            }
                        }
                    },
                    label = {
                        val place = uiState.locality
                        Text(
                            if (uiState.localOnly && place != null) {
                                stringResource(R.string.news_local_with_place, place)
                            } else {
                                stringResource(R.string.news_local)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Le categorie non si applicano alle notizie locali (ricerca per località)
                if (!uiState.localOnly) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(CATEGORIES) { (key, labelRes) ->
                            FilterChip(
                                selected = uiState.selectedCategory == key,
                                onClick = { viewModel.onCategorySelected(key) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                }
            }

            // Contenuto principale
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> LoadingState()
                    uiState.articles.isEmpty() && uiState.error != null -> ErrorState(
                        message = stringResource(R.string.error_loading),
                        onRetry = viewModel::retry
                    )
                    uiState.articles.isEmpty() -> EmptyState(
                        icon = Icons.Default.Search,
                        message = stringResource(R.string.news_empty)
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.articles, key = { it.id }) { article ->
                            ArticleCard(
                                article = article,
                                isFavorite = article.id in uiState.favoriteIds,
                                onToggleFavorite = { viewModel.onToggleFavorite(article) }
                            )
                        }
                    }
                }
            }
        }
    }
}
