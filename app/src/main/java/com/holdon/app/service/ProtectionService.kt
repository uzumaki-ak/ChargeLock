/**
 * File: ProtectionService.kt
 * Purpose: Main foreground service that orchestrates all detectors
 * This is the HEART of the app - manages all detection logic
 */
package com.holdon.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.holdon.app.MainActivity
import com.holdon.app.R
import com.holdon.app.data.local.AlarmSoundManager
import com.holdon.app.data.local.PreferencesManager
import com.holdon.app.data.model.AlarmType
import com.holdon.app.data.model.DetectionSettings
import com.holdon.app.service.detectors.*
import com.holdon.app.util.Constants
import com.holdon.app.util.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Foreground service that runs protection detectors
 * Keeps running even when app is closed
 */
class ProtectionService : Service() {

    companion object {
        private const val TAG = "ProtectionService"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START_PROTECTION = "com.holdon.app.START_PROTECTION"
        const val ACTION_STOP_PROTECTION = "com.holdon.app.STOP_PROTECTION"
        const val ACTION_DISMISS_ALARM = "com.holdon.app.DISMISS_ALARM"
    }

    // Service scope for coroutines
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Managers
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var alarmSoundManager: AlarmSoundManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmPlayer: AlarmPlayer

    // Detectors
    private var powerDetector: PowerUnplugDetector? = null
    private var bluetoothDetector: BluetoothDisconnectDetector? = null
    private var headphoneDetector: HeadphoneUnplugDetector? = null
    private var proximityDetector: ProximityDetector? = null

    // State
    private var isAlarmActive = false
    private var currentSettings: DetectionSettings? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize managers
        preferencesManager = PreferencesManager(this)
        alarmSoundManager = AlarmSoundManager(this)
        notificationHelper = NotificationHelper(this)
        alarmPlayer = AlarmPlayer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_PROTECTION -> {
                startProtection()
            }
            ACTION_STOP_PROTECTION -> {
                stopProtection()
            }
            ACTION_DISMISS_ALARM -> {
                dismissAlarm()
            }
            else -> {
                // Service restarted by system, restore protection if it was active
                serviceScope.launch {
                    val wasActive = preferencesManager.isProtectionActiveFlow.first()
                    if (wasActive) {
                        startProtection()
                    } else {
                        stopSelf()
                    }
                }
            }
        }

        // START_STICKY ensures service is restarted if killed by system
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        // Cleanup all detectors
        cleanupDetectors()

        // Stop alarm if playing
        alarmPlayer.release()

        // Cancel all coroutines
        serviceScope.cancel()
    }

    /**
     * Starts protection with current settings
     */
    private fun startProtection() {
        serviceScope.launch {
            try {
                // Load current settings
                currentSettings = preferencesManager.detectionSettingsFlow.first()

                // Validate settings
                if (currentSettings?.hasAnyDetectionEnabled() != true) {
                    Log.w(TAG, "No detections enabled, stopping service")
                    stopSelf()
                    return@launch
                }

                // Start foreground service with notification
                val notification = createServiceNotification()
                startForeground(NOTIFICATION_ID, notification)

                // Initialize and start detectors
                setupDetectors(currentSettings!!)

                // Mark protection as active
                preferencesManager.setProtectionActive(true)

                Log.d(TAG, "Protection started successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting protection", e)
                stopSelf()
            }
        }
    }

    /**
     * Stops protection and all detectors
     */
    private fun stopProtection() {
        serviceScope.launch {
            // Stop all detectors
            cleanupDetectors()

            // Stop alarm if playing
            if (isAlarmActive) {
                alarmPlayer.stopAlarm()
                isAlarmActive = false
            }

            // Mark protection as inactive
            preferencesManager.setProtectionActive(false)

            // Stop foreground service
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

            Log.d(TAG, "Protection stopped")
        }
    }

    /**
     * Sets up all enabled detectors based on settings
     */
    private fun setupDetectors(settings: DetectionSettings) {
        // Cleanup existing detectors first
        cleanupDetectors()

        // Power detector
        if (settings.powerDetectionEnabled) {
            powerDetector = PowerUnplugDetector(this, serviceScope).apply {
                onAlarmTriggered = ::handleAlarmTriggered
                startDetection()
            }
            Log.d(TAG, "Power detector enabled")
        }

        // Bluetooth detector
        if (settings.bluetoothDetectionEnabled) {
            bluetoothDetector = BluetoothDisconnectDetector(
                context = this,
                coroutineScope = serviceScope,
                monitoredDevices = settings.monitoredBluetoothDevices,
                debounceSeconds = settings.bluetoothDebounceSeconds
            ).apply {
                onAlarmTriggered = ::handleAlarmTriggered
                startDetection()
            }
            Log.d(TAG, "Bluetooth detector enabled (debounce: ${settings.bluetoothDebounceSeconds}s)")
        }

        // Headphone detector
        if (settings.headphoneDetectionEnabled) {
            headphoneDetector = HeadphoneUnplugDetector(
                context = this,
                coroutineScope = serviceScope,
                debounceSeconds = settings.headphoneDebounceSeconds
            ).apply {
                onAlarmTriggered = ::handleAlarmTriggered
                startDetection()
            }
            Log.d(TAG, "Headphone detector enabled (debounce: ${settings.headphoneDebounceSeconds}s)")
        }

        // Proximity detector
        if (settings.proximityDetectionEnabled) {
            proximityDetector = ProximityDetector(
                context = this,
                coroutineScope = serviceScope,
                debounceSeconds = settings.proximityDebounceSeconds
            ).apply {
                onAlarmTriggered = ::handleAlarmTriggered
                startDetection()
            }
            Log.d(TAG, "Proximity detector enabled (debounce: ${settings.proximityDebounceSeconds}s)")
        }
    }

    /**
     * Cleans up all detectors
     */
    private fun cleanupDetectors() {
        powerDetector?.cleanup()
        bluetoothDetector?.cleanup()
        headphoneDetector?.cleanup()
        proximityDetector?.cleanup()

        powerDetector = null
        bluetoothDetector = null
        headphoneDetector = null
        proximityDetector = null

        Log.d(TAG, "All detectors cleaned up")
    }

    /**
     * Handles alarm trigger from any detector
     * @param alarmType Type of alarm that was triggered
     */
    private fun handleAlarmTriggered(alarmType: AlarmType) {
        if (isAlarmActive) {
            Log.w(TAG, "Alarm already active, ignoring trigger")
            return
        }

        Log.e(TAG, "🚨 ALARM TRIGGERED: $alarmType")

        // Get alarm sound URI
        val soundUri = alarmSoundManager.getAlarmSoundUri(currentSettings?.customAlarmSoundUri)

        // Play alarm
        val success = alarmPlayer.playAlarm(soundUri)
        if (success) {
            isAlarmActive = true

            // Show alarm notification
            showAlarmNotification(alarmType)

            // Launch alarm screen (MainActivity will handle showing biometric prompt)
            launchAlarmScreen(alarmType)
        } else {
            Log.e(TAG, "Failed to play alarm sound")
        }
    }

    /**
     * Dismisses the alarm
     */
    private fun dismissAlarm() {
        if (!isAlarmActive) {
            Log.w(TAG, "No active alarm to dismiss")
            return
        }

        Log.d(TAG, "Dismissing alarm")

        // Stop alarm sound
        alarmPlayer.stopAlarm()
        isAlarmActive = false

        // Remove alarm notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(Constants.ALARM_NOTIFICATION_ID)

        // Update service notification
        val notification = createServiceNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Creates foreground service notification
     */
    private fun createServiceNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build enabled detections text
        val enabledDetections = currentSettings?.getEnabledDetections() ?: emptyList()
        val detectionText = when {
            enabledDetections.isEmpty() -> "No detections active"
            enabledDetections.size == 1 -> enabledDetections[0].getDisplayName()
            else -> "Multiple detections active"
        }

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_SERVICE_ID)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(detectionText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Cannot be dismissed by user
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    /**
     * Shows alarm notification with high priority
     */
    private fun showAlarmNotification(alarmType: AlarmType) {
        notificationHelper.showAlarmNotification(
            alarmType = alarmType,
            onDismissIntent = createDismissIntent()
        )
    }

    /**
     * Launches MainActivity to show alarm screen
     */
    private fun launchAlarmScreen(alarmType: AlarmType) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_ALARM_TRIGGERED, true)
            putExtra(Constants.EXTRA_ALARM_TYPE, alarmType.name)
        }
        startActivity(intent)
    }

    /**
     * Creates pending intent for dismissing alarm
     */
    private fun createDismissIntent(): PendingIntent {
        val intent = Intent(this, ProtectionService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}