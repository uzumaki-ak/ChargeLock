/**
 * File: ProximityDetector.kt
 * Purpose: Detects when phone is picked up from face-down position
 * EXPERIMENTAL - Uses proximity sensor to detect movement
 */
package com.holdon.app.service.detectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.holdon.app.data.model.AlarmType
import kotlinx.coroutines.CoroutineScope

/**
 * Detects phone being picked up from face-down position
 * Uses proximity sensor to detect when phone is moved
 */
class ProximityDetector(
    context: Context,
    coroutineScope: CoroutineScope,
    private val debounceSeconds: Int = 2
) : BaseDetector(context, coroutineScope) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var proximitySensor: Sensor? = null

    private var isProximityNear = false
    private var wasProximityNear = false

    /**
     * Sensor event listener for proximity sensor
     */
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                handleProximitySensorChange(it)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Not needed for proximity sensor
        }
    }

    override fun startDetection() {
        if (isActive) {
            Log.w(TAG, "Detection already active")
            return
        }

        // Get proximity sensor
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.e(TAG, "Proximity sensor not available on this device")
            return
        }

        // Register sensor listener
        sensorManager.registerListener(
            sensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        isActive = true
        Log.d(TAG, "Detection started (waiting for phone to be placed face-down)")
    }

    override fun stopDetection() {
        if (!isActive) return

        try {
            sensorManager.unregisterListener(sensorEventListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering sensor", e)
        }

        cancelDebounce()
        isActive = false
        Log.d(TAG, "Detection stopped")
    }

    override fun getAlarmType(): AlarmType = AlarmType.PROXIMITY_CHANGE

    /**
     * Handles proximity sensor value changes
     * @param event Sensor event containing proximity value
     */
    private fun handleProximitySensorChange(event: SensorEvent) {
        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f

        // Near = object close to sensor (phone face-down on table)
        // Far = no object near sensor (phone picked up or face-up)
        isProximityNear = distance < maxRange / 2

        // Log for debugging
        if (isProximityNear != wasProximityNear) {
            Log.d(TAG, "Proximity changed: ${if (isProximityNear) "NEAR (face-down)" else "FAR (picked up)"}")
        }

        // Check for transition from near to far (phone picked up)
        if (wasProximityNear && !isProximityNear) {
            Log.w(TAG, "Phone picked up from face-down position! Starting debounce...")
            triggerAlarmWithDebounce(debounceSeconds)
        }

        // Check for transition from far to near (phone placed down)
        if (!wasProximityNear && isProximityNear) {
            Log.d(TAG, "Phone placed face-down, monitoring active")
            cancelDebounce() // Cancel any pending alarm
        }

        wasProximityNear = isProximityNear
    }

    /**
     * Gets current proximity state
     * @return True if phone is face-down (proximity sensor covered)
     */
    fun isPhoneFaceDown(): Boolean {
        return isProximityNear
    }
}