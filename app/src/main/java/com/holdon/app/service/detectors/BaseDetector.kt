/**
 * File: BaseDetector.kt
 * Purpose: Abstract base class for all detection implementations
 * Provides common functionality like debouncing and callback management
 */
package com.holdon.app.service.detectors

import android.content.Context
import android.util.Log
import com.holdon.app.data.model.AlarmType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Base class for all detector implementations
 * Handles common functionality like debouncing and alarm triggering
 */
abstract class BaseDetector(
    protected val context: Context,
    protected val coroutineScope: CoroutineScope
) {

    protected val TAG: String = this::class.java.simpleName

    /**
     * Callback invoked when an alarm should be triggered
     */
    var onAlarmTriggered: ((AlarmType) -> Unit)? = null

    /**
     * Job for debounce delay
     */
    private var debounceJob: Job? = null

    /**
     * Whether the detector is currently active
     */
    protected var isActive = false

    /**
     * Starts monitoring for this detection type
     * Subclasses must implement their specific detection logic
     */
    abstract fun startDetection()

    /**
     * Stops monitoring
     * Subclasses must implement cleanup logic
     */
    abstract fun stopDetection()

    /**
     * Returns the alarm type this detector is responsible for
     */
    abstract fun getAlarmType(): AlarmType

    /**
     * Triggers alarm with debounce delay
     * @param debounceSeconds Delay in seconds before actually triggering alarm
     */
    protected fun triggerAlarmWithDebounce(debounceSeconds: Int) {
        // Cancel any existing debounce
        debounceJob?.cancel()

        if (debounceSeconds <= 0) {
            // No debounce, trigger immediately
            triggerAlarmImmediate()
            return
        }

        // Start new debounce timer
        debounceJob = coroutineScope.launch {
            Log.d(TAG, "Debounce started: ${debounceSeconds}s")
            delay(debounceSeconds * 1000L)

            // If we reach here, the condition persisted for the full debounce time
            Log.d(TAG, "Debounce completed, triggering alarm")
            triggerAlarmImmediate()
        }
    }

    /**
     * Cancels pending debounce timer
     * Called when the triggering condition is no longer true
     */
    protected fun cancelDebounce() {
        debounceJob?.cancel()
        debounceJob = null
        Log.d(TAG, "Debounce cancelled")
    }

    /**
     * Triggers alarm immediately without debounce
     */
    protected fun triggerAlarmImmediate() {
        val alarmType = getAlarmType()
        Log.w(TAG, "ALARM TRIGGERED: $alarmType")
        onAlarmTriggered?.invoke(alarmType)
    }

    /**
     * Cleanup method called when detector is destroyed
     */
    open fun cleanup() {
        cancelDebounce()
        stopDetection()
    }
}