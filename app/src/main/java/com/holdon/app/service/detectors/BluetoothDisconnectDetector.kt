/**
 * File: BluetoothDisconnectDetector.kt
 * Purpose: Detects when Bluetooth devices disconnect
 * MODERATE RELIABILITY - requires debounce to avoid false alarms
 */
package com.holdon.app.service.detectors

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.holdon.app.data.model.AlarmType
import kotlinx.coroutines.CoroutineScope

/**
 * Detects Bluetooth device disconnection
 * Monitors specified devices or all connected devices
 */
class BluetoothDisconnectDetector(
    context: Context,
    coroutineScope: CoroutineScope,
    private val monitoredDevices: Set<String>, // MAC addresses
    private val debounceSeconds: Int = 15
) : BaseDetector(context, coroutineScope) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val connectedDevices = mutableSetOf<String>()

    /**
     * BroadcastReceiver for Bluetooth connection state changes
     */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { handleDeviceConnected(it) }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { handleDeviceDisconnected(it) }
                }
            }
        }
    }

    override fun startDetection() {
        if (isActive) {
            Log.w(TAG, "Detection already active")
            return
        }

        // Check if Bluetooth is available
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not available on this device")
            return
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth is disabled")
            return
        }

        // Check for Bluetooth permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth permission not granted")
                return
            }
        }

        // Get currently connected devices
        scanCurrentlyConnectedDevices()

        // Register broadcast receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        context.registerReceiver(bluetoothReceiver, filter)
        isActive = true

        Log.d(TAG, "Detection started (monitoring ${monitoredDevices.size} devices, " +
                "currently connected: ${connectedDevices.size})")
    }

    override fun stopDetection() {
        if (!isActive) return

        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }

        cancelDebounce()
        connectedDevices.clear()
        isActive = false

        Log.d(TAG, "Detection stopped")
    }

    override fun getAlarmType(): AlarmType = AlarmType.BLUETOOTH_DISCONNECT

    /**
     * Scans for currently connected Bluetooth devices
     * Uses BluetoothProfile to get connected devices
     */
    private fun scanCurrentlyConnectedDevices() {
        try {
            connectedDevices.clear()

            // Get A2DP (audio) profile connections
            bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                    try {
                        proxy?.connectedDevices?.forEach { device ->
                            connectedDevices.add(device.address)
                            Log.d(TAG, "Currently connected: ${device.name} (${device.address})")
                        }
                        bluetoothAdapter?.closeProfileProxy(profile, proxy)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Permission error scanning devices", e)
                    }
                }

                override fun onServiceDisconnected(profile: Int) {}
            }, BluetoothProfile.A2DP)

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning connected devices", e)
        }
    }

    /**
     * Handles device connection event
     */
    private fun handleDeviceConnected(device: BluetoothDevice) {
        try {
            val deviceAddress = device.address
            val deviceName = device.name ?: "Unknown"

            Log.d(TAG, "Device connected: $deviceName ($deviceAddress)")

            // Add to connected list
            connectedDevices.add(deviceAddress)

            // Cancel any pending alarm for this device
            if (shouldMonitorDevice(deviceAddress)) {
                cancelDebounce()
                Log.d(TAG, "Monitored device reconnected, alarm cancelled")
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission error handling connection", e)
        }
    }

    /**
     * Handles device disconnection event
     */
    private fun handleDeviceDisconnected(device: BluetoothDevice) {
        try {
            val deviceAddress = device.address
            val deviceName = device.name ?: "Unknown"

            Log.d(TAG, "Device disconnected: $deviceName ($deviceAddress)")

            // Remove from connected list
            connectedDevices.remove(deviceAddress)

            // Check if we should trigger alarm
            if (shouldMonitorDevice(deviceAddress)) {
                Log.w(TAG, "Monitored device disconnected! Starting debounce...")
                triggerAlarmWithDebounce(debounceSeconds)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission error handling disconnection", e)
        }
    }

    /**
     * Checks if a device should be monitored
     * @param deviceAddress MAC address of the device
     * @return True if device should be monitored
     */
    private fun shouldMonitorDevice(deviceAddress: String): Boolean {
        // If no specific devices are set, monitor all
        if (monitoredDevices.isEmpty()) {
            return true
        }

        // Otherwise, only monitor specified devices
        return monitoredDevices.contains(deviceAddress)
    }

    /**
     * Gets list of currently connected monitored devices
     */
    fun getConnectedMonitoredDevices(): Set<String> {
        return if (monitoredDevices.isEmpty()) {
            connectedDevices
        } else {
            connectedDevices.intersect(monitoredDevices)
        }
    }
}