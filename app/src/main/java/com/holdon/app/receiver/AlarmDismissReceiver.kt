/**
 * File: AlarmDismissReceiver.kt
 * Purpose: Receives alarm dismiss actions from notifications
 * Handles alarm dismissal when user acts on notification
 */
package com.holdon.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.holdon.app.service.ProtectionService

/**
 * Receives alarm dismiss intents
 * Triggers service to stop alarm
 */
class AlarmDismissReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmDismissReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm dismiss action received")

        // Send dismiss action to service
        val serviceIntent = Intent(context, ProtectionService::class.java).apply {
            action = ProtectionService.ACTION_DISMISS_ALARM
        }

        context.startService(serviceIntent)
    }
}