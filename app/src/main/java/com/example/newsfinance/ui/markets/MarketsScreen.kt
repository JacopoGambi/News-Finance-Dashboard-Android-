package com.example.newsfinance.ui.markets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.ui.components.AddAlertDialog
import com.example.newsfinance.ui.components.CryptoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    modifier: Modifier = Modifier,
    onCryptoClick: (cryptoId: String, currency: String) -> Unit = { _, _ -> },
    viewModel: MarketsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Crypto per cui è aperto il dialog di aggiunta alert
    var dialogCrypto by remember { mutableStateOf<Crypto?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    dialogCrypto?.let { crypto ->
        AddAlertDialog(
            cryptoName = crypto.name,
            currentPrice = crypto.currentPrice,
            currency = uiState.selectedCurrency,
            onConfirm = { threshold, above -> viewModel.onAddAlert(crypto, threshold, above) },
            onDismiss = { dialogCrypto = null }
        )
    }

    val isInitialLoad = uiState.isLoading && uiState.cryptos.isEmpty()
    val isRefreshing = uiState.isLoading && !isInitialLoad

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Selettore valuta
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Valuta:",
                    style = MaterialTheme.typography.labelMedium
                )
                listOf("usd" to "USD", "eur" to "EUR").forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedCurrency == key,
                        onClick = { viewModel.onCurrencyChanged(key) },
                        label = { Text(label) }
                    )
                }
            }

            // Contenuto principale
            if (isInitialLoad) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.cryptos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nessuna crypto disponibile.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.cryptos, key = { it.id }) { crypto ->
                                CryptoCard(
                                    crypto = crypto,
                                    vsCurrency = uiState.selectedCurrency,
                                    isWatchlisted = crypto.id in uiState.watchlistIds,
                                    onToggleWatchlist = { viewModel.onToggleWatchlist(crypto) },
                                    onBellClick = { dialogCrypto = crypto },
                                    onClick = { onCryptoClick(crypto.id, uiState.selectedCurrency) }
                                )
                            }
                        }
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
