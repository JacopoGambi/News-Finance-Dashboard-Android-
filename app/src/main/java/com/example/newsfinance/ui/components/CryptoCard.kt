package com.example.newsfinance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsfinance.domain.model.Crypto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CryptoCard(
    crypto: Crypto,
    modifier: Modifier = Modifier,
    vsCurrency: String = "usd",
    isWatchlisted: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null,
    onBellClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = crypto.imageUrl,
                contentDescription = crypto.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = crypto.symbol.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCryptoPrice(crypto.currentPrice, vsCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                val change = crypto.priceChangePercentage24h
                if (change != null) {
                    val isPositive = change >= 0
                    Text(
                        text = "${if (isPositive) "+" else ""}${"%.2f".format(change)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
            if (onToggleWatchlist != null || onBellClick != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (onToggleWatchlist != null) {
                        IconButton(
                            onClick = onToggleWatchlist,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Filled.Star
                                              else Icons.Filled.StarBorder,
                                contentDescription = if (isWatchlisted) "Rimuovi dalla watchlist"
                                                     else "Aggiungi alla watchlist",
                                tint = if (isWatchlisted) Color(0xFFFFC107)
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (onBellClick != null) {
                        IconButton(
                            onClick = onBellClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (crypto.alertThreshold != null)
                                                  Icons.Filled.Notifications
                                              else Icons.Filled.NotificationsNone,
                                contentDescription = "Imposta soglia di prezzo",
                                tint = if (crypto.alertThreshold != null)
                                           MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCryptoPrice(price: Double?, vsCurrency: String): String {
    if (price == null) return "N/A"
    val nf = if (vsCurrency.equals("eur", ignoreCase = true)) {
        NumberFormat.getCurrencyInstance(Locale.GERMANY)
    } else {
        NumberFormat.getCurrencyInstance(Locale.US)
    }
    return nf.format(price)
}
