package com.example.newsfinance.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.newsfinance.ui.components.CryptoCard
import com.example.newsfinance.ui.components.ErrorState
import com.example.newsfinance.ui.components.LoadingState
import com.example.newsfinance.util.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCryptoClick: (cryptoId: String, currency: String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    // Richiede il permesso alla prima visualizzazione se non ancora concesso
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            permissionRequested = true
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Recupera il paese dalla posizione quando il permesso è concesso
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            val countryCode = LocationHelper.getCountryCode(context)
            if (countryCode != null) {
                viewModel.setDetectedCountry(countryCode)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    // Rifiuto permanente: permesso richiesto almeno una volta e non più proponibile
    val isPermanentlyDenied = permissionRequested &&
        !locationPermissionState.status.isGranted &&
        !locationPermissionState.status.shouldShowRationale

    val isInitialLoad = uiState.isLoading && uiState.articles.isEmpty() && uiState.cryptos.isEmpty()
    val isRefreshing = uiState.isLoading && !isInitialLoad

    val isEmptyError = uiState.error != null &&
        uiState.articles.isEmpty() && uiState.cryptos.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        if (isInitialLoad) {
            LoadingState()
        } else if (isEmptyError) {
            // Errore senza dati in cache: schermata di errore con possibilità di riprovare
            ErrorState(
                message = stringResource(R.string.error_loading),
                onRetry = viewModel::refresh
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isPermanentlyDenied) {
                        item {
                            LocationPermissionBanner(context = context)
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.home_latest_news),
                                style = MaterialTheme.typography.titleMedium
                            )
                            uiState.detectedCountry?.let { detected ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(detected.uppercase()) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    items(
                        items = uiState.articles.take(5),
                        key = { it.id }
                    ) { article ->
                        ArticleCard(article = article)
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_top_crypto),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(
                        items = uiState.cryptos,
                        key = { it.id }
                    ) { crypto ->
                        CryptoCard(
                            crypto = crypto,
                            vsCurrency = uiState.currency,
                            onClick = { onCryptoClick(crypto.id, uiState.currency) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LocationPermissionBanner(context: Context) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.location_enable_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.location_enable_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            TextButton(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            ) {
                Text(stringResource(R.string.action_settings))
            }
        }
    }
}

