package com.example.newsfinance.ui.markets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.R
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.ui.components.AddAlertDialog
import com.example.newsfinance.ui.components.CryptoGroupCard
import com.example.newsfinance.ui.components.EmptyState
import com.example.newsfinance.ui.components.ErrorState
import com.example.newsfinance.ui.components.LoadingState

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

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Text(
                text = stringResource(R.string.nav_markets),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            )

            // Selettore valuta
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.markets_currency),
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
                LoadingState()
            } else if (uiState.cryptos.isEmpty() && uiState.error != null) {
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
                    if (uiState.cryptos.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.CurrencyBitcoin,
                            message = stringResource(R.string.markets_empty)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Righe raggruppate in un unico contenitore con divider
                            item {
                                CryptoGroupCard(
                                    cryptos = uiState.cryptos,
                                    vsCurrency = uiState.selectedCurrency,
                                    watchlistIds = uiState.watchlistIds,
                                    onToggleWatchlist = { viewModel.onToggleWatchlist(it) },
                                    onBellClick = { dialogCrypto = it },
                                    onClick = { onCryptoClick(it.id, uiState.selectedCurrency) }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}
