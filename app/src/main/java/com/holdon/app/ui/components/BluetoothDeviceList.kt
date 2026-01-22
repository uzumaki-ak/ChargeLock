/**
 * File: BluetoothDeviceList.kt
 * Purpose: Component to display and manage Bluetooth devices
 * Shows paired devices and allows selecting which to monitor
 */
package com.holdon.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.holdon.app.data.model.BluetoothDeviceInfo
import com.holdon.app.data.model.DeviceType

/**
 * List of Bluetooth devices with monitoring toggles
 *
 * @param devices List of available Bluetooth devices
 * @param onDeviceToggle Callback when device monitoring is toggled
 * @param modifier Modifier for customization
 */
@Composable
fun BluetoothDeviceList(
    devices: List<BluetoothDeviceInfo>,
    onDeviceToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (devices.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BluetoothDisabled,
                    contentDescription = "No devices",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No Bluetooth devices found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pair a device in your Bluetooth settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(devices) { device ->
                BluetoothDeviceItem(
                    device = device,
                    onToggle = { enabled ->
                        onDeviceToggle(device.address, enabled)
                    }
                )
                Divider()
            }
        }
    }
}

/**
 * Single Bluetooth device item
 */
@Composable
private fun BluetoothDeviceItem(
    device: BluetoothDeviceInfo,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device icon based on type
        Icon(
            imageVector = getDeviceIcon(device.type),
            contentDescription = device.type.getDisplayName(),
            modifier = Modifier.size(32.dp),
            tint = if (device.isConnected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Device info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.getDisplayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.type.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (device.isConnected) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Monitor toggle
        Checkbox(
            checked = device.isMonitored,
            onCheckedChange = onToggle
        )
    }
}

/**
 * Gets icon for device type
 */
private fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.AUDIO_VIDEO -> Icons.Default.Headphones
        DeviceType.WEARABLE -> Icons.Default.Watch
        DeviceType.PHONE -> Icons.Default.Smartphone
        DeviceType.COMPUTER -> Icons.Default.Computer
        DeviceType.UNKNOWN -> Icons.Default.Bluetooth
    }
}