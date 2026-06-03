package com.example.newsfinance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.newsfinance.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Dialog per aggiungere un alert di prezzo su una crypto.
 * Permette di scegliere la soglia e la direzione (sopra/sotto).
 */
@Composable
fun AddAlertDialog(
    cryptoName: String,
    currentPrice: Double?,
    currency: String,
    onConfirm: (threshold: Double, above: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var above by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alert_new_title, cryptoName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { newValue ->
                        val normalized = newValue.replace(',', '.')
                        val filtered = normalized.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1 && !filtered.startsWith('.')) {
                            input = filtered
                        }
                    },
                    label = { Text(stringResource(R.string.alert_price_in, currency.uppercase())) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    visualTransformation = CurrencyVisualTransformation(currency)
                )
                Text(
                    text = stringResource(R.string.alert_notify_when),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = above,
                        onClick = { above = true },
                        label = { Text(stringResource(R.string.alert_above)) }
                    )
                    FilterChip(
                        selected = !above,
                        onClick = { above = false },
                        label = { Text(stringResource(R.string.alert_below)) }
                    )
                }
                Text(
                    text = stringResource(
                        R.string.alert_current_price,
                        formatPrice(currentPrice, currency)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    input.toDoubleOrNull()?.let { threshold -> onConfirm(threshold, above) }
                    onDismiss()
                }
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
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

/**
 * Formatta l'input numerico come valuta (separatori migliaia/decimali e simbolo)
 * mantenendo corretto il mapping del cursore.
 */
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
