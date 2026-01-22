/**
 * File: NotificationHelper.kt
 * Purpose: Helper for creating and managing notifications
 * Centralizes notification logic for service and alarms
 */
package com.holdon.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.holdon.app.R
import com.holdon.app.data.model.AlarmType

/**
 * Helper class for notification management
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    /**
     * Shows alarm notification with high priority
     * @param alarmType Type of alarm that triggered
     * @param onDismissIntent PendingIntent for dismiss action
     */
    fun showAlarmNotification(
        alarmType: AlarmType,
        onDismissIntent: PendingIntent
    ) {
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALARM_ID)
            .setContentTitle(context.getString(R.string.notification_alarm_title))
            .setContentText(alarmType.getDisplayName())
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(onDismissIntent, true)
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.dismiss_alarm),
                onDismissIntent
            )
            .build()

        notificationManager.notify(Constants.ALARM_NOTIFICATION_ID, notification)
    }

    /**
     * Dismisses alarm notification
     */
    fun dismissAlarmNotification() {
        notificationManager.cancel(Constants.ALARM_NOTIFICATION_ID)
    }
}