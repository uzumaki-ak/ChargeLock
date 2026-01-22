/**
 * File: PreferencesManager.kt
 * Purpose: Manages app preferences using DataStore
 * Provides type-safe access to all app settings with Flow-based updates
 */
package com.holdon.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.holdon.app.data.model.DetectionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension property to create DataStore instance
 * Lazy initialization ensures single instance per app
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "holdon_preferences")

/**
 * Manages all app preferences using Jetpack DataStore
 * Provides coroutine-based async access to settings
 */
class PreferencesManager(private val context: Context) {

    // Preference keys
    private object Keys {
        val POWER_DETECTION = booleanPreferencesKey("power_detection_enabled")
        val BLUETOOTH_DETECTION = booleanPreferencesKey("bluetooth_detection_enabled")
        val HEADPHONE_DETECTION = booleanPreferencesKey("headphone_detection_enabled")
        val PROXIMITY_DETECTION = booleanPreferencesKey("proximity_detection_enabled")

        val BLUETOOTH_DEBOUNCE = intPreferencesKey("bluetooth_debounce_seconds")
        val HEADPHONE_DEBOUNCE = intPreferencesKey("headphone_debounce_seconds")
        val PROXIMITY_DEBOUNCE = intPreferencesKey("proximity_debounce_seconds")

        val MONITORED_DEVICES = stringSetPreferencesKey("monitored_bluetooth_devices")
        val REQUIRE_BIOMETRIC = booleanPreferencesKey("require_biometric")
        val CUSTOM_ALARM_SOUND = stringPreferencesKey("custom_alarm_sound_uri")
        val START_ON_BOOT = booleanPreferencesKey("start_on_boot")

        val PROTECTION_ACTIVE = booleanPreferencesKey("protection_active")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    /**
     * Flow of current detection settings
     * Automatically updates UI when settings change
     */
    val detectionSettingsFlow: Flow<DetectionSettings> = context.dataStore.data.map { prefs ->
        DetectionSettings(
            powerDetectionEnabled = prefs[Keys.POWER_DETECTION] ?: true,
            bluetoothDetectionEnabled = prefs[Keys.BLUETOOTH_DETECTION] ?: false,
            headphoneDetectionEnabled = prefs[Keys.HEADPHONE_DETECTION] ?: false,
            proximityDetectionEnabled = prefs[Keys.PROXIMITY_DETECTION] ?: false,
            bluetoothDebounceSeconds = prefs[Keys.BLUETOOTH_DEBOUNCE] ?: 15,
            headphoneDebounceSeconds = prefs[Keys.HEADPHONE_DEBOUNCE] ?: 3,
            proximityDebounceSeconds = prefs[Keys.PROXIMITY_DEBOUNCE] ?: 2,
            monitoredBluetoothDevices = prefs[Keys.MONITORED_DEVICES] ?: emptySet(),
            requireBiometric = prefs[Keys.REQUIRE_BIOMETRIC] ?: true,
            customAlarmSoundUri = prefs[Keys.CUSTOM_ALARM_SOUND],
            startOnBoot = prefs[Keys.START_ON_BOOT] ?: false
        )
    }

    /**
     * Flow of protection active state
     * Used to sync UI across app components
     */
    val isProtectionActiveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PROTECTION_ACTIVE] ?: false
    }

    /**
     * Flow to check if this is the first app launch
     * Used to show onboarding or tutorial
     */
    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.FIRST_LAUNCH] ?: true
    }

    /**
     * Updates detection settings
     * @param settings New detection settings to save
     */
    suspend fun updateDetectionSettings(settings: DetectionSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.POWER_DETECTION] = settings.powerDetectionEnabled
            prefs[Keys.BLUETOOTH_DETECTION] = settings.bluetoothDetectionEnabled
            prefs[Keys.HEADPHONE_DETECTION] = settings.headphoneDetectionEnabled
            prefs[Keys.PROXIMITY_DETECTION] = settings.proximityDetectionEnabled
            prefs[Keys.BLUETOOTH_DEBOUNCE] = settings.bluetoothDebounceSeconds
            prefs[Keys.HEADPHONE_DEBOUNCE] = settings.headphoneDebounceSeconds
            prefs[Keys.PROXIMITY_DEBOUNCE] = settings.proximityDebounceSeconds
            prefs[Keys.MONITORED_DEVICES] = settings.monitoredBluetoothDevices
            prefs[Keys.REQUIRE_BIOMETRIC] = settings.requireBiometric
            settings.customAlarmSoundUri?.let { prefs[Keys.CUSTOM_ALARM_SOUND] = it }
            prefs[Keys.START_ON_BOOT] = settings.startOnBoot
        }
    }

    /**
     * Sets protection active state
     * @param isActive True to enable protection, false to disable
     */
    suspend fun setProtectionActive(isActive: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PROTECTION_ACTIVE] = isActive
        }
    }

    /**
     * Marks that the app has been launched (for onboarding)
     */
    suspend fun markFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[Keys.FIRST_LAUNCH] = false
        }
    }

    /**
     * Updates individual detection enable/disable
     */
    suspend fun updatePowerDetection(enabled: Boolean) {
        context.dataStore.edit { it[Keys.POWER_DETECTION] = enabled }
    }

    suspend fun updateBluetoothDetection(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLUETOOTH_DETECTION] = enabled }
    }

    suspend fun updateHeadphoneDetection(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HEADPHONE_DETECTION] = enabled }
    }

    suspend fun updateProximityDetection(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PROXIMITY_DETECTION] = enabled }
    }

    /**
     * Updates monitored Bluetooth devices
     */
    suspend fun updateMonitoredDevices(devices: Set<String>) {
        context.dataStore.edit { it[Keys.MONITORED_DEVICES] = devices }
    }

    /**
     * Adds a device to monitored list
     */
    suspend fun addMonitoredDevice(deviceAddress: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.MONITORED_DEVICES] ?: emptySet()
            prefs[Keys.MONITORED_DEVICES] = current + deviceAddress
        }
    }

    /**
     * Removes a device from monitored list
     */
    suspend fun removeMonitoredDevice(deviceAddress: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.MONITORED_DEVICES] ?: emptySet()
            prefs[Keys.MONITORED_DEVICES] = current - deviceAddress
        }
    }
}