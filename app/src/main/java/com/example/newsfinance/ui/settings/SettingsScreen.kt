package com.example.newsfinance.ui.settings

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.R
import com.example.newsfinance.ui.theme.appCardBorder
import com.example.newsfinance.ui.theme.appCardColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

private val themes = listOf(
    "system" to R.string.theme_system,
    "light" to R.string.theme_light,
    "dark" to R.string.theme_dark
)

private val currencies = listOf(
    "usd" to "USD",
    "eur" to "EUR"
)

private val intervals = listOf(
    15 to R.string.interval_15,
    30 to R.string.interval_30,
    60 to R.string.interval_60
)

private val languages = listOf(
    "it" to R.string.lang_it,
    "en" to R.string.lang_en,
    "es" to R.string.lang_es,
    "fr" to R.string.lang_fr
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.uiState.collectAsStateWithLifecycle()

    // Su API < 33 POST_NOTIFICATIONS non è un permesso runtime: status sarà sempre Granted
    val notificationsPermissionState = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )

    // Lingua attualmente applicata all'app (o quella di sistema se non impostata)
    val currentLanguage = AppCompatDelegate.getApplicationLocales()[0]?.language
        ?: java.util.Locale.getDefault().language

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // --- Sezione Aspetto ---
        SectionHeader(stringResource(R.string.settings_section_appearance))

        SettingsField(
            label = stringResource(R.string.settings_theme),
            options = themes.map { (key, res) -> key to stringResource(res) },
            selectedKey = prefs.themeMode,
            onSelected = viewModel::onThemeModeChanged
        )

        // --- Sezione Lingua ---
        SectionHeader(stringResource(R.string.settings_section_language))

        SettingsField(
            label = stringResource(R.string.settings_language),
            options = languages.map { (tag, res) -> tag to stringResource(res) },
            selectedKey = currentLanguage,
            onSelected = { tag ->
                // Applica il locale all'intera UI e persiste la scelta per le notizie
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
                viewModel.onLanguageChanged(tag)
            }
        )

        // --- Sezione Mercati ---
        SectionHeader(stringResource(R.string.settings_section_markets))

        SettingsField(
            label = stringResource(R.string.settings_currency),
            options = currencies,
            selectedKey = prefs.preferredCurrency,
            onSelected = viewModel::onCurrencyChanged
        )

        // --- Sezione Notifiche ---
        SectionHeader(stringResource(R.string.settings_section_notifications))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = appCardColors(),
            border = appCardBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.settings_enable_notifications),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !notificationsPermissionState.status.isGranted) {
                            notificationsPermissionState.launchPermissionRequest()
                        }
                        viewModel.onNotificationsToggled(enabled)
                    }
                )
            }
        }

        SettingsField(
            label = stringResource(R.string.settings_update_interval),
            options = intervals.map { (k, res) -> k.toString() to stringResource(res) },
            selectedKey = prefs.updateIntervalMinutes.toString(),
            onSelected = { viewModel.onIntervalChanged(it.toInt()) },
            enabled = prefs.notificationsEnabled
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 2.dp, top = 10.dp, bottom = 2.dp)
    )
}

/** Riga-card impostazione: etichetta piccola + valore + caret, apre un menu a tendina. */
@Composable
private fun SettingsField(
    label: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedKey }?.second ?: selectedKey

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.5f)
                .clickable(enabled = enabled) { expanded = true },
            shape = RoundedCornerShape(14.dp),
            colors = appCardColors(),
            border = appCardBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
