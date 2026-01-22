/**
 * File: HoldOnApplication.kt
 * Purpose: Application class that initializes app-wide components
 * This is the entry point of the app, called before any activity is created
 */
package com.holdon.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.holdon.app.util.Constants
import com.holdon.app.R

/**
 * Custom Application class for HoldOn
 * Handles app-level initialization like notification channels
 */
class HoldOnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channels for Android 8.0+
        createNotificationChannels()
    }

    /**
     * Creates notification channels required by the app
     * Android 8.0+ requires notification channels for all notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Channel for foreground service notification
            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_SERVICE_ID,
                getString(R.string.notification_channel_service_name),
                NotificationManager.IMPORTANCE_LOW // Low importance to avoid disturbing user
            ).apply {
                description = getString(R.string.notification_channel_service_desc)
                setShowBadge(false) // Don't show badge on app icon
                enableVibration(false)
                setSound(null, null) // Silent notification
            }

            // Channel for alarm notifications
            val alarmChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ALARM_ID,
                getString(R.string.notification_channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH // High importance for alarm
            ).apply {
                description = getString(R.string.notification_channel_alarm_desc)
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                lightColor = ContextCompat.getColor(this@HoldOnApplication, R.color.alarm_red)
            }

            // Register channels with system
            notificationManager?.createNotificationChannel(serviceChannel)
            notificationManager?.createNotificationChannel(alarmChannel)
        }
    }
}
