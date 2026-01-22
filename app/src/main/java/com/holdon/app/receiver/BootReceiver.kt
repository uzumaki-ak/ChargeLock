/**
 * File: BootReceiver.kt
 * Purpose: Receives boot completed broadcast to restart protection
 * Allows app to automatically start protection after device reboot
 */
package com.holdon.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.holdon.app.data.local.PreferencesManager
import com.holdon.app.service.ProtectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives system boot completed broadcast
 * Restarts protection service if enabled in settings
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        // Quickboot actions are not standard Android constants but used by some manufacturers
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        private const val ACTION_HTC_QUICKBOOT_POWERON = "com.htc.intent.action.QUICKBOOT_POWERON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction = intent.action
        if (receivedAction != Intent.ACTION_BOOT_COMPLETED &&
            receivedAction != ACTION_QUICKBOOT_POWERON &&
            receivedAction != ACTION_HTC_QUICKBOOT_POWERON) {
            return
        }

        Log.d(TAG, "Boot completed ($receivedAction), checking if protection should restart")

        // Use goAsync to allow async work in BroadcastReceiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val preferencesManager = PreferencesManager(context)
                val settings = preferencesManager.detectionSettingsFlow.first()
                val wasActive = preferencesManager.isProtectionActiveFlow.first()

                // Only restart if:
                // 1. Start on boot is enabled
                // 2. Protection was active before reboot
                if (settings.startOnBoot && wasActive) {
                    Log.d(TAG, "Starting protection service after boot")

                    val serviceIntent = Intent(context, ProtectionService::class.java).apply {
                        action = ProtectionService.ACTION_START_PROTECTION
                    }

                    context.startForegroundService(serviceIntent)
                } else {
                    Log.d(TAG, "Protection not configured to start on boot")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in boot receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
