/**
 * File: DetectionCard.kt
 * Purpose: Card component for each detection type
 * Shows detection status and allows toggling
 */
package com.holdon.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.holdon.app.data.model.AlarmType

/**
 * Card displaying a detection type with toggle
 *
 * @param title Detection type name
 * @param description Detection description
 * @param icon Icon for the detection type
 * @param enabled Whether detection is enabled
 * @param onToggle Callback when user toggles
 * @param modifier Modifier for customization
 */
@Composable
fun DetectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Toggle switch
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * Helper function to get icon for alarm type
 */
fun getIconForAlarmType(alarmType: AlarmType): ImageVector {
    return when (alarmType) {
        AlarmType.POWER_DISCONNECT -> Icons.Default.Power
        AlarmType.BLUETOOTH_DISCONNECT -> Icons.Default.Bluetooth
        AlarmType.HEADPHONE_UNPLUG -> Icons.Default.Headphones
        AlarmType.PROXIMITY_CHANGE -> Icons.Default.PhoneAndroid
    }
}