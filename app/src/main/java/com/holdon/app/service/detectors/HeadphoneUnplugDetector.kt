/**
 * File: HeadphoneUnplugDetector.kt
 * Purpose: Detects when wired headphones are unplugged
 * GOOD RELIABILITY - works with both 3.5mm and USB-C, needs small debounce
 */
package com.holdon.app.service.detectors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.holdon.app.data.model.AlarmType
import kotlinx.coroutines.CoroutineScope

/**
 * Detects wired headphone disconnection
 * Uses AudioManager callbacks for modern Android versions
 */
class HeadphoneUnplugDetector(
    context: Context,
    coroutineScope: CoroutineScope,
    private val debounceSeconds: Int = 3
) : BaseDetector(context, coroutineScope) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var headphonesConnected = false

    /**
     * Legacy broadcast receiver for older Android versions
     */
    private val headphoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                when (state) {
                    0 -> handleHeadphoneDisconnected() // Unplugged
                    1 -> handleHeadphoneConnected()    // Plugged in
                }
            }
        }
    }

    /**
     * Modern audio device callback for Android 6.0+
     */
    private val audioDeviceCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                addedDevices?.forEach { device ->
                    if (isWiredHeadphone(device)) {
                        handleHeadphoneConnected()
                    }
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                removedDevices?.forEach { device ->
                    if (isWiredHeadphone(device)) {
                        handleHeadphoneDisconnected()
                    }
                }
            }
        }
    } else {
        null
    }

    override fun startDetection() {
        if (isActive) {
            Log.w(TAG, "Detection already active")
            return
        }

        // Check current headphone state
        checkCurrentHeadphoneState()

        // Use modern callback if available, otherwise use legacy receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            Log.d(TAG, "Using AudioDeviceCallback (modern)")
        } else {
            val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            context.registerReceiver(headphoneReceiver, filter)
            Log.d(TAG, "Using BroadcastReceiver (legacy)")
        }

        isActive = true
        Log.d(TAG, "Detection started (headphones connected: $headphonesConnected)")
    }

    override fun stopDetection() {
        if (!isActive) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
                audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            } else {
                context.unregisterReceiver(headphoneReceiver)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callbacks", e)
        }

        cancelDebounce()
        isActive = false
        Log.d(TAG, "Detection stopped")
    }

    override fun getAlarmType(): AlarmType = AlarmType.HEADPHONE_UNPLUG

    /**
     * Checks current headphone connection state
     */
    private fun checkCurrentHeadphoneState() {
        headphonesConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Modern method: Check audio devices
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            devices.any { isWiredHeadphone(it) }
        } else {
            // Legacy method: Not reliably available without broadcast
            @Suppress("DEPRECATION")
            audioManager.isWiredHeadsetOn
        }

        Log.d(TAG, "Current headphone state: $headphonesConnected")
    }

    /**
     * Checks if an audio device is a wired headphone
     * @param device AudioDeviceInfo to check
     * @return True if device is wired headphone
     */
    private fun isWiredHeadphone(device: AudioDeviceInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_USB_HEADSET
        } else {
            false
        }
    }

    /**
     * Handles headphone connection event
     */
    private fun handleHeadphoneConnected() {
        Log.d(TAG, "Headphones connected")
        headphonesConnected = true
        cancelDebounce() // Cancel any pending alarm
    }

    /**
     * Handles headphone disconnection event
     */
    private fun handleHeadphoneDisconnected() {
        if (headphonesConnected) {
            Log.w(TAG, "Headphones disconnected! Starting debounce...")
            triggerAlarmWithDebounce(debounceSeconds)
        }
        headphonesConnected = false
    }
}