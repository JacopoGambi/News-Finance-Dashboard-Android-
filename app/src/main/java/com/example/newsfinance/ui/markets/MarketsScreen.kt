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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.ui.components.CryptoCard
import java.text.NumberFormat
import java.util.Locale

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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = dialogInput,
                        onValueChange = { newValue ->
                            val normalized = newValue.replace(',', '.')
                            val filtered = normalized.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1 && !filtered.startsWith('.')) {
                                dialogInput = filtered
                            }
                        },
                        label = { Text("Prezzo in ${uiState.selectedCurrency.uppercase()}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        visualTransformation = CurrencyVisualTransformation(uiState.selectedCurrency)
                    )
                    Text(
                        text = "Prezzo attuale: ${formatPrice(crypto.currentPrice, uiState.selectedCurrency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    if (crypto.alertThreshold != null) {
                        Text(
                            text = "Soglia attuale: ${formatPrice(crypto.alertThreshold, uiState.selectedCurrency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
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
                                        dialogInput = crypto.alertThreshold?.let {
                                            if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
                                        } ?: ""
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

private fun formatPrice(price: Double?, currency: String): String {
    if (price == null) return "N/A"
    val nf = if (currency.equals("eur", ignoreCase = true)) {
        NumberFormat.getCurrencyInstance(Locale.GERMANY)
    } else {
        NumberFormat.getCurrencyInstance(Locale.US)
    }
    return nf.format(price)
}

private class CurrencyVisualTransformation(
    private val currency: String
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val isEur = currency.equals("eur", ignoreCase = true)
        val thousandSep = if (isEur) '.' else ','
        val decimalSep = if (isEur) ',' else '.'
        val prefix = if (isEur) "" else "$"
        val suffix = if (isEur) " €" else ""

        val dotIndex = raw.indexOf('.')
        val intPart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
        val decPart = if (dotIndex >= 0) raw.substring(dotIndex + 1) else null

        val origToTrans = IntArray(raw.length + 1)
        val sb = StringBuilder()

        if (prefix.isNotEmpty()) sb.append(prefix)

        val intLen = intPart.length
        for (i in intPart.indices) {
            origToTrans[i] = sb.length
            sb.append(intPart[i])
            val posFromRight = intLen - 1 - i
            if (posFromRight > 0 && posFromRight % 3 == 0) {
                sb.append(thousandSep)
            }
        }

        if (decPart != null) {
            origToTrans[dotIndex] = sb.length
            sb.append(decimalSep)
            for (i in decPart.indices) {
                origToTrans[dotIndex + 1 + i] = sb.length
                sb.append(decPart[i])
            }
        }

        origToTrans[raw.length] = sb.length

        if (suffix.isNotEmpty()) sb.append(suffix)

        val formatted = sb.toString()
        val transToOrig = IntArray(formatted.length + 1)
        var oi = 0
        for (ti in 0..formatted.length) {
            while (oi < raw.length && origToTrans[oi + 1] <= ti) {
                oi++
            }
            transToOrig[ti] = oi
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                origToTrans[offset.coerceIn(0, raw.length)]

            override fun transformedToOriginal(offset: Int): Int =
                transToOrig[offset.coerceIn(0, formatted.length)]
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
