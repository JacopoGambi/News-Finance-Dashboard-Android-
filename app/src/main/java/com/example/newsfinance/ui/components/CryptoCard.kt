package com.example.newsfinance.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.newsfinance.R
import com.example.newsfinance.domain.model.Crypto
import com.example.newsfinance.ui.theme.MonoNumbers
import com.example.newsfinance.ui.theme.appCardBorder
import com.example.newsfinance.ui.theme.appCardColors
import com.example.newsfinance.util.CurrencyFormatter
import java.util.Locale

private val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

/**
 * Card singola per una crypto (usata in Home e Preferiti).
 * Mantiene callback e API esistenti; il layout interno è condiviso con [CryptoGroupCard].
 */
@Composable
fun CryptoCard(
    crypto: Crypto,
    modifier: Modifier = Modifier,
    vsCurrency: String = "usd",
    isWatchlisted: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null,
    onBellClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = appCardColors(),
        border = appCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        CryptoRow(
            crypto = crypto,
            vsCurrency = vsCurrency,
            isWatchlisted = isWatchlisted,
            onToggleWatchlist = onToggleWatchlist,
            onBellClick = onBellClick,
            onClick = onClick
        )
    }
}

/**
 * Più crypto raggruppate in un unico contenitore con divider tra le righe (usata in Mercati).
 */
@Composable
fun CryptoGroupCard(
    cryptos: List<Crypto>,
    modifier: Modifier = Modifier,
    vsCurrency: String = "usd",
    watchlistIds: Set<String> = emptySet(),
    onToggleWatchlist: ((Crypto) -> Unit)? = null,
    onBellClick: ((Crypto) -> Unit)? = null,
    onClick: ((Crypto) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = appCardColors(),
        border = appCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        cryptos.forEachIndexed { index, crypto ->
            CryptoRow(
                crypto = crypto,
                vsCurrency = vsCurrency,
                isWatchlisted = crypto.id in watchlistIds,
                onToggleWatchlist = onToggleWatchlist?.let { { it(crypto) } },
                onBellClick = onBellClick?.let { { it(crypto) } },
                onClick = onClick?.let { { it(crypto) } }
            )
            if (index < cryptos.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun CryptoRow(
    crypto: Crypto,
    vsCurrency: String,
    isWatchlisted: Boolean,
    onToggleWatchlist: (() -> Unit)?,
    onBellClick: (() -> Unit)?,
    onClick: (() -> Unit)?
) {
    val imageFallback = rememberVectorPainter(Icons.Default.BrokenImage)

    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 12.dp)

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = crypto.imageUrl,
            contentDescription = crypto.name,
            placeholder = imageFallback,
            error = imageFallback,
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

        Sparkline(prices = crypto.sparkline7d)

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.format(crypto.currentPrice, vsCurrency),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = MonoNumbers,
                fontWeight = FontWeight.SemiBold
            )
            crypto.priceChangePercentage24h?.let { change ->
                ChangePill(
                    change = change,
                    modifier = Modifier.padding(top = 3.dp)
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
                            contentDescription = stringResource(
                                if (isWatchlisted) R.string.crypto_remove_watchlist
                                else R.string.crypto_add_watchlist
                            ),
                            tint = if (isWatchlisted) MaterialTheme.colorScheme.tertiary
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
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = stringResource(R.string.crypto_add_alert),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
