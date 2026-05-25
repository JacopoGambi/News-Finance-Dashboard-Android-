package com.example.newsfinance.ui.markets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.ui.components.CryptoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    modifier: Modifier = Modifier,
    viewModel: MarketsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stato locale per il dialogo di impostazione soglia
    var dialogCrypto by remember { mutableStateOf<Crypto?>(null) }
    var dialogInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    // AlertDialog per la soglia di prezzo
    dialogCrypto?.let { crypto ->
        AlertDialog(
            onDismissRequest = { dialogCrypto = null },
            title = { Text("Soglia alert — ${crypto.name}") },
            text = {
                OutlinedTextField(
                    value = dialogInput,
                    onValueChange = { dialogInput = it },
                    label = { Text("Prezzo in ${uiState.selectedCurrency.uppercase()}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    supportingText = {
                        if (crypto.alertThreshold != null) {
                            Text("Soglia attuale: ${crypto.alertThreshold}")
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogInput.toDoubleOrNull()?.let { threshold ->
                            viewModel.onSetAlertThreshold(crypto.id, threshold)
                        }
                        dialogCrypto = null
                    }
                ) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { dialogCrypto = null }) { Text("Annulla") }
            }
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
                                    onBellClick = {
                                        dialogInput = crypto.alertThreshold?.toString() ?: ""
                                        dialogCrypto = crypto
                                    }
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
