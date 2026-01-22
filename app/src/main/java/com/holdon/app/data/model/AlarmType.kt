/**
 * File: AlarmType.kt
 * Purpose: Enum defining all possible alarm trigger types
 * Used to identify why an alarm was triggered
 */
package com.holdon.app.data.model

/**
 * Represents different types of alarms that can be triggered
 * Each type corresponds to a specific detection method
 */
enum class AlarmType {
    /**
     * Alarm triggered when charging cable is disconnected
     * Most reliable detection method
     */
    POWER_DISCONNECT,

    /**
     * Alarm triggered when Bluetooth device disconnects
     * May have false positives due to signal interference
     */
    BLUETOOTH_DISCONNECT,

    /**
     * Alarm triggered when wired headphones are unplugged
     * Works with both 3.5mm and USB-C headphones
     */
    HEADPHONE_UNPLUG,

    /**
     * Alarm triggered when phone is picked up from face-down position
     * Uses proximity sensor to detect movement
     */
    PROXIMITY_CHANGE;

    /**
     * Returns a human-readable description of the alarm type
     * Used for notifications and UI display
     */
    fun getDisplayName(): String {
        return when (this) {
            POWER_DISCONNECT -> "Charging cable unplugged"
            BLUETOOTH_DISCONNECT -> "Bluetooth device disconnected"
            HEADPHONE_UNPLUG -> "Headphones unplugged"
            PROXIMITY_CHANGE -> "Phone moved from face-down position"
        }
    }
}