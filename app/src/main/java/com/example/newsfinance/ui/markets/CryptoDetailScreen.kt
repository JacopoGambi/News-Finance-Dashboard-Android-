package com.example.newsfinance.ui.markets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CryptoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.imageUrl != null) {
                        AsyncImage(
                            model = uiState.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = if (uiState.cryptoName.isNotEmpty())
                            "${uiState.cryptoName} (${uiState.cryptoSymbol})"
                        else "Dettaglio"
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Prezzo e variazione calcolata sull'intervallo selezionato
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.currentPrice != null) {
                Text(
                    text = formatPrice(uiState.currentPrice, "usd"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Calcola la variazione % dal primo all'ultimo punto del grafico caricato;
            // se il grafico non è ancora pronto usa il valore 24h come fallback
            val rangeChange: Double? = run {
                val points = uiState.chartPoints
                if (points.size >= 2) {
                    val first = points.first().second
                    val last = points.last().second
                    if (first != 0.0) (last - first) / first * 100.0 else null
                } else {
                    uiState.priceChange24h
                }
            }
            val rangeLabel = uiState.selectedRange.label
            if (rangeChange != null) {
                val positive = rangeChange >= 0
                Text(
                    text = "${if (positive) "+" else ""}${"%.2f".format(rangeChange)}% ($rangeLabel)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (positive) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selettore intervallo temporale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartTimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = uiState.selectedRange == range,
                        onClick = { viewModel.onRangeSelected(range) },
                        label = { Text(range.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Area del grafico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                val chartError = uiState.error
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    chartError != null -> Text(
                        text = chartError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    uiState.chartPoints.isEmpty() -> Text(
                        text = "Nessun dato disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    else -> PriceLineChart(
                        points = uiState.chartPoints,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PriceLineChart(
    points: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val minPrice = points.minOf { it.second }
        val maxPrice = points.maxOf { it.second }
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)
        val count = points.size
        val xStep = size.width / (count - 1).toFloat()

        fun xOf(i: Int) = i * xStep
        fun yOf(price: Double) = (size.height * (1.0 - (price - minPrice) / priceRange)).toFloat()

        // Percorso di riempimento sotto la linea
        val fillPath = Path().apply {
            moveTo(xOf(0), size.height)
            points.forEachIndexed { i, (_, price) -> lineTo(xOf(i), yOf(price)) }
            lineTo(xOf(count - 1), size.height)
            close()
        }
        drawPath(fillPath, color = fillColor)

        // Linea del prezzo
        val linePath = Path().apply {
            points.forEachIndexed { i, (_, price) ->
                if (i == 0) moveTo(xOf(0), yOf(price)) else lineTo(xOf(i), yOf(price))
            }
        }
        drawPath(
            linePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun formatPrice(price: Double?, currency: String): String {
    if (price == null) return "N/A"
    val nf = if (currency.equals("eur", ignoreCase = true))
        NumberFormat.getCurrencyInstance(Locale.GERMANY)
    else
        NumberFormat.getCurrencyInstance(Locale.US)
    return nf.format(price)
}
