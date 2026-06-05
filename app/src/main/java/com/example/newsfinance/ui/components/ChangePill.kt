package com.example.newsfinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.newsfinance.ui.theme.MonoNumbers
import com.example.newsfinance.ui.theme.NegativeRed
import com.example.newsfinance.ui.theme.NegativeRedDark
import com.example.newsfinance.ui.theme.PositiveGreen
import com.example.newsfinance.ui.theme.PositiveGreenDark

/**
 * Badge della variazione percentuale: pill con sfondo tinto verde/rosso e freccia.
 * Colore semantico coerente in light/dark; cifre monospazio per allineamento.
 */
@Composable
fun ChangePill(
    change: Double,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val positive = change >= 0
    val color = if (positive) {
        if (isDark) PositiveGreenDark else PositiveGreen
    } else {
        if (isDark) NegativeRedDark else NegativeRed
    }
    val arrow = if (positive) "▲" else "▼"
    val sign = if (positive) "+" else ""

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = if (isDark) 0.20f else 0.12f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = "${if (showArrow) "$arrow " else ""}$sign${"%.2f".format(change)}%",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = MonoNumbers,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
