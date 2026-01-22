/**
 * File: DetectionSettings.kt
 * Purpose: Data class holding all detection configuration settings
 * Used to store and retrieve user preferences for each detection type
 */
package com.holdon.app.data.model

/**
 * Holds all user-configurable detection settings
 * This is the single source of truth for protection configuration
 */
data class DetectionSettings(
    /**
     * Enable/disable power disconnect detection
     * Default: true (core feature)
     */
    val powerDetectionEnabled: Boolean = true,

    /**
     * Enable/disable Bluetooth disconnect detection
     * Default: false (optional, may have false alarms)
     */
    val bluetoothDetectionEnabled: Boolean = false,

    /**
     * Enable/disable headphone unplug detection
     * Default: false (optional)
     */
    val headphoneDetectionEnabled: Boolean = false,

    /**
     * Enable/disable proximity detection (face-down mode)
     * Default: false (experimental feature)
     */
    val proximityDetectionEnabled: Boolean = false,

    /**
     * Debounce time in seconds for Bluetooth detection
     * Prevents false alarms from brief disconnections
     * Range: 5-30 seconds, Default: 15
     */
    val bluetoothDebounceSeconds: Int = 15,

    /**
     * Debounce time in seconds for headphone detection
     * Prevents false alarms from accidental touches
     * Range: 2-10 seconds, Default: 3
     */
    val headphoneDebounceSeconds: Int = 3,

    /**
     * Debounce time in seconds for proximity detection
     * Prevents false alarms from brief sensor changes
     * Range: 1-5 seconds, Default: 2
     */
    val proximityDebounceSeconds: Int = 2,

    /**
     * List of Bluetooth device addresses to monitor
     * Empty list means monitor all connected devices
     */
    val monitoredBluetoothDevices: Set<String> = emptySet(),

    /**
     * Require biometric authentication to dismiss alarm
     * Default: true (recommended for security)
     */
    val requireBiometric: Boolean = true,

    /**
     * Custom alarm sound URI (null = use default)
     * Points to a file in /res/raw or user-selected audio
     */
    val customAlarmSoundUri: String? = null,

    /**
     * Start protection automatically on device boot
     * Default: false (user choice)
     */
    val startOnBoot: Boolean = false
) {
    /**
     * Returns true if at least one detection method is enabled
     * Used to validate before starting protection service
     */
    fun hasAnyDetectionEnabled(): Boolean {
        return powerDetectionEnabled ||
                bluetoothDetectionEnabled ||
                headphoneDetectionEnabled ||
                proximityDetectionEnabled
    }

    /**
     * Returns a list of enabled detection types
     * Useful for displaying active protections to user
     */
    fun getEnabledDetections(): List<AlarmType> {
        return buildList {
            if (powerDetectionEnabled) add(AlarmType.POWER_DISCONNECT)
            if (bluetoothDetectionEnabled) add(AlarmType.BLUETOOTH_DISCONNECT)
            if (headphoneDetectionEnabled) add(AlarmType.HEADPHONE_UNPLUG)
            if (proximityDetectionEnabled) add(AlarmType.PROXIMITY_CHANGE)
        }
    }
}