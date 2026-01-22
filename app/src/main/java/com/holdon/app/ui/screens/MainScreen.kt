/**
 * File: MainScreen.kt
 * Purpose: Main screen of the app
 * Shows protection status and detection controls
 */
package com.holdon.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holdon.app.data.model.AlarmType
import com.holdon.app.ui.components.DetectionCard
import com.holdon.app.ui.components.ProtectionToggle
import com.holdon.app.ui.components.getIconForAlarmType
import com.holdon.app.ui.viewmodel.MainViewModel

/**
 * Main screen composable
 * Entry point of the app UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HoldOn",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Main protection toggle
            ProtectionToggle(
                isProtected = uiState.isProtectionActive,
                onToggle = { viewModel.toggleProtection() }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Section title
            Text(
                text = "Detection Methods",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Power detection card
            DetectionCard(
                title = "Power Disconnect",
                description = "Alert when charging cable is unplugged",
                icon = getIconForAlarmType(AlarmType.POWER_DISCONNECT),
                enabled = uiState.detectionSettings.powerDetectionEnabled,
                onToggle = { viewModel.updatePowerDetection(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bluetooth detection card
            DetectionCard(
                title = "Bluetooth Disconnect",
                description = "Alert when paired device disconnects (may have false alarms)",
                icon = getIconForAlarmType(AlarmType.BLUETOOTH_DISCONNECT),
                enabled = uiState.detectionSettings.bluetoothDetectionEnabled,
                onToggle = { viewModel.updateBluetoothDetection(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Headphone detection card
            DetectionCard(
                title = "Headphone Unplug",
                description = "Alert when wired headphones are removed",
                icon = getIconForAlarmType(AlarmType.HEADPHONE_UNPLUG),
                enabled = uiState.detectionSettings.headphoneDetectionEnabled,
                onToggle = { viewModel.updateHeadphoneDetection(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Proximity detection card
            DetectionCard(
                title = "Proximity (Face Down)",
                description = "Alert when phone is picked up from face-down position",
                icon = getIconForAlarmType(AlarmType.PROXIMITY_CHANGE),
                enabled = uiState.detectionSettings.proximityDetectionEnabled,
                onToggle = { viewModel.updateProximityDetection(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Warning card if no detections enabled
            if (!uiState.detectionSettings.hasAnyDetectionEnabled()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Enable at least one detection method to start protection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}