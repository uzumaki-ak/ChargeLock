/**
 * File: SettingsRepository.kt
 * Purpose: Repository layer for settings data
 * Provides a clean API for accessing and modifying app settings
 */
package com.holdon.app.data.repository

import com.holdon.app.data.local.AlarmSoundManager
import com.holdon.app.data.local.PreferencesManager
import com.holdon.app.data.model.DetectionSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing app settings
 * Acts as a single source of truth and abstracts data sources
 */
class SettingsRepository(
    private val preferencesManager: PreferencesManager,
    private val alarmSoundManager: AlarmSoundManager
) {

    /**
     * Flow of current detection settings
     * UI observes this to react to settings changes
     */
    val detectionSettings: Flow<DetectionSettings> = preferencesManager.detectionSettingsFlow

    /**
     * Flow of protection active state
     */
    val isProtectionActive: Flow<Boolean> = preferencesManager.isProtectionActiveFlow

    /**
     * Flow of first launch state
     */
    val isFirstLaunch: Flow<Boolean> = preferencesManager.isFirstLaunchFlow

    /**
     * Updates all detection settings at once
     * @param settings New settings to apply
     */
    suspend fun updateSettings(settings: DetectionSettings) {
        preferencesManager.updateDetectionSettings(settings)
    }

    /**
     * Enables or disables protection
     * @param isActive True to enable, false to disable
     */
    suspend fun setProtectionActive(isActive: Boolean) {
        preferencesManager.setProtectionActive(isActive)
    }

    /**
     * Toggles individual detection types
     */
    suspend fun togglePowerDetection(enabled: Boolean) {
        preferencesManager.updatePowerDetection(enabled)
    }

    suspend fun toggleBluetoothDetection(enabled: Boolean) {
        preferencesManager.updateBluetoothDetection(enabled)
    }

    suspend fun toggleHeadphoneDetection(enabled: Boolean) {
        preferencesManager.updateHeadphoneDetection(enabled)
    }

    suspend fun toggleProximityDetection(enabled: Boolean) {
        preferencesManager.updateProximityDetection(enabled)
    }

    /**
     * Manages monitored Bluetooth devices
     */
    suspend fun addMonitoredDevice(deviceAddress: String) {
        preferencesManager.addMonitoredDevice(deviceAddress)
    }

    suspend fun removeMonitoredDevice(deviceAddress: String) {
        preferencesManager.removeMonitoredDevice(deviceAddress)
    }

    /**
     * Gets available alarm sounds for picker
     */
    fun getAvailableAlarmSounds() = alarmSoundManager.getAvailableAlarmSounds()

    /**
     * Marks first launch as complete
     */
    suspend fun completeFirstLaunch() {
        preferencesManager.markFirstLaunchComplete()
    }
}