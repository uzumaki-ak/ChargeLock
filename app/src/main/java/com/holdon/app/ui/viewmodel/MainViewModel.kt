/**
 * File: MainViewModel.kt
 * Purpose: ViewModel for main screen logic
 * Manages protection state and detector settings
 */
package com.holdon.app.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.holdon.app.data.local.AlarmSoundManager
import com.holdon.app.data.local.PreferencesManager
import com.holdon.app.data.model.DetectionSettings
import com.holdon.app.data.repository.SettingsRepository
import com.holdon.app.service.ProtectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for main screen
 */
data class MainUiState(
    val isProtectionActive: Boolean = false,
    val detectionSettings: DetectionSettings = DetectionSettings(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for MainActivity
 * Manages protection state and settings
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(
        PreferencesManager(application),
        AlarmSoundManager(application)
    )

    /**
     * UI state flow combining all necessary state
     */
    val uiState: StateFlow<MainUiState> = combine(
        repository.isProtectionActive,
        repository.detectionSettings
    ) { isActive, settings ->
        MainUiState(
            isProtectionActive = isActive,
            detectionSettings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(isLoading = true)
    )

    /**
     * Toggles protection on/off
     */
    fun toggleProtection() {
        viewModelScope.launch {
            val currentState = uiState.value
            val newState = !currentState.isProtectionActive

            if (newState) {
                startProtection()
            } else {
                stopProtection()
            }
        }
    }

    /**
     * Starts protection service
     */
    private fun startProtection() {
        viewModelScope.launch {
            try {
                // Validate settings
                val settings = uiState.value.detectionSettings
                if (!settings.hasAnyDetectionEnabled()) {
                    // TODO: Show error to user
                    return@launch
                }

                // Start service
                val intent = Intent(getApplication(), ProtectionService::class.java).apply {
                    action = ProtectionService.ACTION_START_PROTECTION
                }
                getApplication<Application>().startForegroundService(intent)

                // Update state
                repository.setProtectionActive(true)

            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    /**
     * Stops protection service
     */
    private fun stopProtection() {
        viewModelScope.launch {
            try {
                val intent = Intent(getApplication(), ProtectionService::class.java).apply {
                    action = ProtectionService.ACTION_STOP_PROTECTION
                }
                getApplication<Application>().startService(intent)

                repository.setProtectionActive(false)

            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    /**
     * Updates individual detection settings
     */
    fun updatePowerDetection(enabled: Boolean) {
        viewModelScope.launch {
            repository.togglePowerDetection(enabled)
        }
    }

    fun updateBluetoothDetection(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleBluetoothDetection(enabled)
        }
    }

    fun updateHeadphoneDetection(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleHeadphoneDetection(enabled)
        }
    }

    fun updateProximityDetection(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleProximityDetection(enabled)
        }
    }
}