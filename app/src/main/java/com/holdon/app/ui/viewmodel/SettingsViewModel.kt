/**
 * File: SettingsViewModel.kt
 * Purpose: ViewModel for settings screen
 * Manages all app settings and preferences
 */
package com.holdon.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.holdon.app.data.local.AlarmSoundManager
import com.holdon.app.data.local.PreferencesManager
import com.holdon.app.data.model.BluetoothDeviceInfo
import com.holdon.app.data.model.DetectionSettings
import com.holdon.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for settings screen
 */
data class SettingsUiState(
    val settings: DetectionSettings = DetectionSettings(),
    val availableDevices: List<BluetoothDeviceInfo> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel for settings management
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(
        PreferencesManager(application),
        AlarmSoundManager(application)
    )

    private val _availableDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())

    /**
     * UI state flow
     */
    val uiState: StateFlow<SettingsUiState> = repository.detectionSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetectionSettings()
        ).let { settingsFlow ->
            MutableStateFlow(
                SettingsUiState(
                    settings = settingsFlow.value,
                    availableDevices = _availableDevices.value
                )
            )
        }

    /**
     * Updates all settings at once
     */
    fun updateSettings(settings: DetectionSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    /**
     * Adds a Bluetooth device to monitored list
     */
    fun addMonitoredDevice(deviceAddress: String) {
        viewModelScope.launch {
            repository.addMonitoredDevice(deviceAddress)
        }
    }

    /**
     * Removes a Bluetooth device from monitored list
     */
    fun removeMonitoredDevice(deviceAddress: String) {
        viewModelScope.launch {
            repository.removeMonitoredDevice(deviceAddress)
        }
    }

    /**
     * Updates available Bluetooth devices list
     */
    fun updateAvailableDevices(devices: List<BluetoothDeviceInfo>) {
        _availableDevices.value = devices
    }
}