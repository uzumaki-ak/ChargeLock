/**
 * File: AlarmSoundManager.kt
 * Purpose: Manages alarm sound selection and playback
 * Handles custom alarm sounds and default system sounds
 */
package com.holdon.app.data.local

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.holdon.app.R

/**
 * Manages alarm sound selection and retrieval
 * Provides access to system alarm sounds and custom user sounds
 */
class AlarmSoundManager(private val context: Context) {

    /**
     * Gets the URI for the alarm sound to play
     * Returns custom sound if set, otherwise returns default alarm
     * @param customSoundUri Custom sound URI from user preferences
     * @return URI of the sound to play
     */
    fun getAlarmSoundUri(customSoundUri: String?): Uri {
        return if (customSoundUri != null) {
            try {
                Uri.parse(customSoundUri)
            } catch (e: Exception) {
                // If custom sound is invalid, fall back to default
                getDefaultAlarmSound()
            }
        } else {
            getDefaultAlarmSound()
        }
    }

    /**
     * Gets the default system alarm sound
     * @return URI of the default alarm sound
     */
    fun getDefaultAlarmSound(): Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Uri.parse("android.resource://${context.packageName}/" + RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
    }

    /**
     * Gets a list of available system alarm sounds
     * Used for sound picker UI
     * @return List of alarm sound URIs with display names
     */
    fun getAvailableAlarmSounds(): List<Pair<String, Uri>> {
        val sounds = mutableListOf<Pair<String, Uri>>()
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)

        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            sounds.add(Pair(title, uri))
        }
        cursor.close()

        return sounds
    }

    /**
     * Validates if a URI points to a valid audio file
     * @param uri URI to validate
     * @return True if the URI is valid and accessible
     */
    fun isValidSoundUri(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the display name for a sound URI
     * @param uri URI of the sound
     * @return Human-readable name of the sound
     */
    fun getSoundDisplayName(uri: Uri): String {
        return try {
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.getTitle(context)
        } catch (e: Exception) {
            "Custom Sound"
        }
    }
}
