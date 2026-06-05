package com.example.newsfinance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsfinance.ui.theme.MonoNumbers
import com.example.newsfinance.util.CurrencyFormatter

// Colori brand fissi: la hero resta blu→teal con testo bianco in entrambi i temi.
private val HeroBlue = Color(0xFF1565C0)
private val HeroBlueDark = Color(0xFF0F4C8C)
private val HeroTeal = Color(0xFF00897B)

/**
 * Hero card in cima alla Home: riassume l'andamento aggregato del mercato crypto.
 * Gradiente diagonale, ombra colorata e glow decorativo come da mockup di design.
 * I dati arrivano già calcolati dal ViewModel (capitalizzazione totale, variazione media).
 */
@Composable
fun MarketSummaryCard(
    label: String,
    totalCap: Double?,
    avgChange: Double?,
    currency: String,
    sparkline: List<Double>?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val gradient = Brush.linearGradient(
        0.0f to HeroBlue,
        0.55f to HeroBlueDark,
        1.0f to HeroTeal
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = shape,
                spotColor = HeroBlue,
                ambientColor = HeroBlue
            )
            .clip(shape)
            .background(gradient)
            .padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        // Glow morbido in alto a destra
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 34.dp, y = (-44).dp)
                .size(160.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = CurrencyFormatter.formatCompact(totalCap, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = MonoNumbers,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            if (avgChange != null) {
                val positive = avgChange >= 0
                val arrow = if (positive) "▲" else "▼"
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$arrow ${if (positive) "+" else ""}${"%.2f".format(avgChange)}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = MonoNumbers,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (!sparkline.isNullOrEmpty()) {
            SparkBars(
                values = sparkline,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(96.dp, 40.dp)
            )
        }
    }
}

/** Mini istogramma decorativo per la hero, ricavato dai prezzi recenti. */
@Composable
private fun SparkBars(
    values: List<Double>,
    modifier: Modifier = Modifier,
    barCount: Int = 7
) {
    // Ricampiona la serie a poche barre uniformi
    val sampled = if (values.size <= barCount) values else {
        val step = values.size.toFloat() / barCount
        (0 until barCount).map { values[(it * step).toInt().coerceIn(0, values.lastIndex)] }
    }

    Canvas(modifier = modifier) {
        if (sampled.isEmpty()) return@Canvas
        val min = sampled.min()
        val max = sampled.max()
        val range = (max - min).coerceAtLeast(1e-9)
        val gap = 4f.dp.toPx()
        val barWidth = (size.width - gap * (sampled.size - 1)) / sampled.size
        sampled.forEachIndexed { i, v ->
            val norm = ((v - min) / range).toFloat().coerceIn(0.12f, 1f)
            val barHeight = size.height * norm
            val x = i * (barWidth + gap)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f.dp.toPx())
            )
        }
    }
}
