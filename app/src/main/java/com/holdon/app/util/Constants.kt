/**
 * File: Constants.kt
 * Purpose: Defines all app-wide constants
 * Single source of truth for constant values
 */
package com.holdon.app.util

/**
 * App-wide constants
 */
object Constants {

    // Notification Channels
    const val NOTIFICATION_CHANNEL_SERVICE_ID = "holdon_service_channel"
    const val NOTIFICATION_CHANNEL_ALARM_ID = "holdon_alarm_channel"

    // Notification IDs
    const val SERVICE_NOTIFICATION_ID = 1001
    const val ALARM_NOTIFICATION_ID = 2001

    // Intent Extras
    const val EXTRA_ALARM_TRIGGERED = "extra_alarm_triggered"
    const val EXTRA_ALARM_TYPE = "extra_alarm_type"

    // Debounce Limits
    const val MIN_BLUETOOTH_DEBOUNCE = 5
    const val MAX_BLUETOOTH_DEBOUNCE = 30
    const val MIN_HEADPHONE_DEBOUNCE = 2
    const val MAX_HEADPHONE_DEBOUNCE = 10
    const val MIN_PROXIMITY_DEBOUNCE = 1
    const val MAX_PROXIMITY_DEBOUNCE = 5

    // Permission Request Codes
    const val PERMISSION_REQUEST_BLUETOOTH = 1001
    const val PERMISSION_REQUEST_NOTIFICATION = 1002

    // Shared Preferences Keys (deprecated - using DataStore)
    @Deprecated("Use PreferencesManager with DataStore")
    const val PREFS_NAME = "holdon_prefs"
}