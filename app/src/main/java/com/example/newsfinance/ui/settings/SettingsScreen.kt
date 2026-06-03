package com.example.newsfinance.ui.settings

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newsfinance.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Sezione Lingua ---
        SectionHeader(stringResource(R.string.settings_section_language))

        SettingsDropdown(
            label = stringResource(R.string.settings_language),
            options = languages.map { (tag, res) -> tag to stringResource(res) },
            selectedKey = currentLanguage,
            onSelected = { tag ->
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(tag)
                )
            }
        )

        HorizontalDivider()

        // --- Sezione Mercati ---
        SectionHeader(stringResource(R.string.settings_section_markets))

        SettingsDropdown(
            label = stringResource(R.string.settings_currency),
            options = currencies,
            selectedKey = prefs.preferredCurrency,
            onSelected = viewModel::onCurrencyChanged
        )

        HorizontalDivider()

        // --- Sezione Notifiche ---
        SectionHeader(stringResource(R.string.settings_section_notifications))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.settings_enable_notifications),
                style = MaterialTheme.typography.bodyLarge
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

        SettingsDropdown(
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
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedKey }?.second ?: selectedKey

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelected(key)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
