package com.example.newsfinance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.newsfinance.ui.theme.NegativeRed
import com.example.newsfinance.ui.theme.NegativeRedDark
import com.example.newsfinance.ui.theme.PositiveGreen
import com.example.newsfinance.ui.theme.PositiveGreenDark

/**
 * Mini grafico a linea che riassume l'andamento recente dei prezzi.
 * Si colora di verde se l'ultimo prezzo è ≥ del primo, altrimenti di rosso.
 * Con meno di due punti non disegna nulla (degrada elegantemente).
 */
@Composable
fun Sparkline(
    prices: List<Double>?,
    modifier: Modifier = Modifier,
    width: Int = 54,
    height: Int = 24
) {
    val points = prices.orEmpty()
    val isDark = isSystemInDarkTheme()
    val positive = points.size < 2 || points.last() >= points.first()
    val color = if (positive) {
        if (isDark) PositiveGreenDark else PositiveGreen
    } else {
        if (isDark) NegativeRedDark else NegativeRed
    }

    Canvas(modifier = modifier.size(width.dp, height.dp)) {
        if (points.size < 2) return@Canvas

        val minPrice = points.min()
        val maxPrice = points.max()
        val range = (maxPrice - minPrice).coerceAtLeast(1e-9)
        val xStep = size.width / (points.size - 1).toFloat()
        val padding = 2f

        fun yOf(price: Double): Float {
            val norm = ((price - minPrice) / range).toFloat()
            return size.height - padding - norm * (size.height - 2 * padding)
        }

        val path = Path().apply {
            points.forEachIndexed { i, price ->
                val x = i * xStep
                val y = yOf(price)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
