/**
 * File: AlarmPlayer.kt
 * Purpose: Handles alarm sound playback with maximum volume override
 * Ensures alarm plays loudly regardless of device volume settings
 */
package com.holdon.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Manages alarm sound playback with volume override and vibration
 * Ensures alarm is loud and noticeable even in silent mode
 */
class AlarmPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var previousAlarmVolume: Int = -1

    companion object {
        private const val TAG = "AlarmPlayer"
        private val VIBRATION_PATTERN = longArrayOf(0, 500, 200, 500, 200, 500) // Pattern: wait, vibrate, pause, repeat
    }

    /**
     * Plays alarm sound with maximum volume override
     * @param soundUri URI of the alarm sound to play
     * @return True if playback started successfully, false otherwise
     */
    fun playAlarm(soundUri: Uri): Boolean {
        try {
            // Stop any existing playback
            stopAlarm()

            // Save current alarm volume
            previousAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

            // Set alarm stream to maximum volume
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                maxVolume,
                0 // No UI flags
            )

            // Create and configure MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                setDataSource(context, soundUri)
                isLooping = true // Loop alarm until dismissed

                setOnPreparedListener {
                    start()
                    Log.d(TAG, "Alarm playback started")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    false
                }

                prepareAsync()
            }

            // Start vibration
            startVibration()

            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm", e)
            return false
        }
    }

    /**
     * Stops alarm playback and restores previous volume
     */
    fun stopAlarm() {
        try {
            // Stop MediaPlayer
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null

            // Restore previous alarm volume if it was saved
            if (previousAlarmVolume != -1) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    previousAlarmVolume,
                    0
                )
                previousAlarmVolume = -1
            }

            // Stop vibration
            stopVibration()

            Log.d(TAG, "Alarm playback stopped")

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm", e)
        }
    }

    /**
     * Starts continuous vibration pattern
     */
    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(
                    VIBRATION_PATTERN,
                    0 // Repeat from index 0 (loop)
                )
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_PATTERN, 0)
            }
            Log.d(TAG, "Vibration started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration", e)
        }
    }

    /**
     * Stops vibration
     */
    private fun stopVibration() {
        try {
            vibrator.cancel()
            Log.d(TAG, "Vibration stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop vibration", e)
        }
    }

    /**
     * Checks if alarm is currently playing
     * @return True if alarm is playing, false otherwise
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    /**
     * Releases all resources
     * Call this when the alarm player is no longer needed
     */
    fun release() {
        stopAlarm()
    }
}