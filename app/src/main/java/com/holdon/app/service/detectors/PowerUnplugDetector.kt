/**
 * File: PowerUnplugDetector.kt
 * Purpose: Detects when charging cable is disconnected
 * MOST RELIABLE detector - no false positives, instant detection
 */
package com.holdon.app.service.detectors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.holdon.app.data.model.AlarmType
import kotlinx.coroutines.CoroutineScope

/**
 * Detects power cable disconnection
 * Uses BroadcastReceiver for instant, reliable detection
 */
class PowerUnplugDetector(
    context: Context,
    coroutineScope: CoroutineScope
) : BaseDetector(context, coroutineScope) {

    private var isCharging = false

    /**
     * BroadcastReceiver that listens for power connection changes
     */
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.d(TAG, "Power disconnected")
                    handlePowerDisconnected()
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    Log.d(TAG, "Power connected")
                    handlePowerConnected()
                }
            }
        }
    }

    override fun startDetection() {
        if (isActive) {
            Log.w(TAG, "Detection already active")
            return
        }

        // Check current charging state
        checkCurrentChargingState()

        // Register broadcast receiver for power state changes
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        context.registerReceiver(powerReceiver, filter)
        isActive = true

        Log.d(TAG, "Detection started (currently charging: $isCharging)")
    }

    override fun stopDetection() {
        if (!isActive) return

        try {
            context.unregisterReceiver(powerReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        isActive = false
        Log.d(TAG, "Detection stopped")
    }

    override fun getAlarmType(): AlarmType = AlarmType.POWER_DISCONNECT

    /**
     * Checks current charging state using BatteryManager
     */
    private fun checkCurrentChargingState() {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)

        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        Log.d(TAG, "Current charging state: $isCharging")
    }

    /**
     * Handles power disconnected event
     * Only triggers alarm if we were previously charging
     */
    private fun handlePowerDisconnected() {
        if (isCharging) {
            // We were charging, now disconnected - TRIGGER ALARM!
            Log.w(TAG, "Charger unplugged while protection active!")
            triggerAlarmImmediate() // No debounce needed - this is reliable
        }
        isCharging = false
    }

    /**
     * Handles power connected event
     * Updates charging state, no alarm needed
     */
    private fun handlePowerConnected() {
        isCharging = true
        Log.d(TAG, "Charger connected")
    }
}