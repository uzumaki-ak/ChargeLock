/**
 * File: BluetoothDeviceInfo.kt
 * Purpose: Data class representing a Bluetooth device
 * Used for displaying and managing paired Bluetooth devices
 */
package com.holdon.app.data.model

/**
 * Represents a Bluetooth device with its basic information
 * Used in the UI to display and select devices to monitor
 */
data class BluetoothDeviceInfo(
    /**
     * Device name (e.g., "AirPods Pro", "Galaxy Watch")
     * May be null if device name is not available
     */
    val name: String?,

    /**
     * Device MAC address (e.g., "00:11:22:33:44:55")
     * Unique identifier for the device
     */
    val address: String,

    /**
     * Device type (e.g., AUDIO_VIDEO, PHONE, WEARABLE)
     * Used to categorize devices and show appropriate icons
     */
    val type: DeviceType,

    /**
     * Whether this device is currently being monitored
     * User can toggle this in settings
     */
    val isMonitored: Boolean = false,

    /**
     * Whether this device is currently connected
     * Updated in real-time
     */
    val isConnected: Boolean = false
) {
    /**
     * Returns display name (use address if name is null)
     * Ensures we always have something to show
     */
    fun getDisplayName(): String {
        return name ?: address
    }
}

/**
 * Enum representing different types of Bluetooth devices
 * Used to categorize devices and show appropriate icons
 */
enum class DeviceType {
    /**
     * Audio/Video devices (headphones, earbuds, speakers)
     */
    AUDIO_VIDEO,

    /**
     * Wearable devices (smartwatches, fitness trackers)
     */
    WEARABLE,

    /**
     * Phone devices (smartphones, tablets)
     */
    PHONE,

    /**
     * Computer devices (laptops, desktops)
     */
    COMPUTER,

    /**
     * Unknown or other device types
     */
    UNKNOWN;

    /**
     * Returns a user-friendly name for the device type
     */
    fun getDisplayName(): String {
        return when (this) {
            AUDIO_VIDEO -> "Audio Device"
            WEARABLE -> "Wearable"
            PHONE -> "Phone"
            COMPUTER -> "Computer"
            UNKNOWN -> "Device"
        }
    }
}