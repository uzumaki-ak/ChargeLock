/**
 * File: PermissionManager.kt
 * Purpose: Manages runtime permission checks and requests
 * Provides easy-to-use permission utilities
 */
package com.holdon.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility class for checking and managing permissions
 */
class PermissionManager(private val context: Context) {

    /**
     * Checks if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Checks if Bluetooth permissions are granted (Android 12+)
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Checks if biometric permission is available
     */
    fun hasBiometricPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.USE_BIOMETRIC
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets list of permissions that need to be requested
     */
    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()

        if (!hasNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!hasBluetoothPermissions() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            missing.add(Manifest.permission.BLUETOOTH_CONNECT)
            missing.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        return missing
    }

    /**
     * Checks if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasNotificationPermission() && hasBluetoothPermissions()
    }
}