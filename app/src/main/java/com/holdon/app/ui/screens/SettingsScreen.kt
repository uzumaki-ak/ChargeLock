/**
 * File: SettingsScreen.kt
 * Purpose: Settings screen for advanced configuration
 * Allows customizing debounce times, alarm sound, etc.
 */
package com.holdon.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holdon.app.ui.components.SettingItem
import com.holdon.app.ui.components.SliderSettingItem
import com.holdon.app.ui.viewmodel.SettingsViewModel
import com.holdon.app.util.Constants

/**
 * Settings screen composable
 * Advanced app configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Alarm Settings Section
            SectionHeader(title = "Alarm Settings")

            SettingItem(
                title = "Alarm Sound",
                description = "Choose custom alarm sound",
                icon = Icons.Default.MusicNote,
                onClick = { /* TODO: Open sound picker */ }
            )

            Divider()

            SettingItem(
                title = "Require Biometric",
                description = "Use fingerprint or face unlock to dismiss alarm",
                icon = Icons.Default.Fingerprint,
                trailingContent = {
                    Switch(
                        checked = uiState.settings.requireBiometric,
                        onCheckedChange = { enabled ->
                            viewModel.updateSettings(
                                uiState.settings.copy(requireBiometric = enabled)
                            )
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detection Settings Section
            SectionHeader(title = "Detection Settings")

            SliderSettingItem(
                title = "Bluetooth Debounce",
                description = "Delay before triggering alarm for Bluetooth disconnect",
                value = uiState.settings.bluetoothDebounceSeconds.toFloat(),
                valueRange = Constants.MIN_BLUETOOTH_DEBOUNCE.toFloat()..Constants.MAX_BLUETOOTH_DEBOUNCE.toFloat(),
                onValueChange = { value ->
                    viewModel.updateSettings(
                        uiState.settings.copy(bluetoothDebounceSeconds = value.toInt())
                    )
                },
                valueLabel = { "${it.toInt()}s" }
            )

            Divider()

            SliderSettingItem(
                title = "Headphone Debounce",
                description = "Delay before triggering alarm for headphone unplug",
                value = uiState.settings.headphoneDebounceSeconds.toFloat(),
                valueRange = Constants.MIN_HEADPHONE_DEBOUNCE.toFloat()..Constants.MAX_HEADPHONE_DEBOUNCE.toFloat(),
                onValueChange = { value ->
                    viewModel.updateSettings(
                        uiState.settings.copy(headphoneDebounceSeconds = value.toInt())
                    )
                },
                valueLabel = { "${it.toInt()}s" }
            )

            Divider()

            SliderSettingItem(
                title = "Proximity Debounce",
                description = "Delay before triggering alarm for proximity change",
                value = uiState.settings.proximityDebounceSeconds.toFloat(),
                valueRange = Constants.MIN_PROXIMITY_DEBOUNCE.toFloat()..Constants.MAX_PROXIMITY_DEBOUNCE.toFloat(),
                onValueChange = { value ->
                    viewModel.updateSettings(
                        uiState.settings.copy(proximityDebounceSeconds = value.toInt())
                    )
                },
                valueLabel = { "${it.toInt()}s" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // General Settings Section
            SectionHeader(title = "General Settings")

            SettingItem(
                title = "Start on Boot",
                description = "Automatically start protection after device restart",
                icon = Icons.Default.PowerSettingsNew,
                trailingContent = {
                    Switch(
                        checked = uiState.settings.startOnBoot,
                        onCheckedChange = { enabled ->
                            viewModel.updateSettings(
                                uiState.settings.copy(startOnBoot = enabled)
                            )
                        }
                    )
                }
            )

            Divider()

            SettingItem(
                title = "About",
                description = "Version and app information",
                icon = Icons.Default.Info,
                onClick = { /* TODO: Show about dialog */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header for grouping settings
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}