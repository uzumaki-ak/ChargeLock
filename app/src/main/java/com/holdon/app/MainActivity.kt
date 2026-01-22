/**
 * File: MainActivity.kt
 * Purpose: Main activity that hosts the Compose UI with navigation
 * Entry point for the app's user interface
 */
package com.holdon.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.holdon.app.ui.screens.MainScreen
import com.holdon.app.ui.screens.SettingsScreen
import com.holdon.app.ui.theme.HoldOnTheme

/**
 * Main Activity for HoldOn app
 * Handles permission requests and hosts the Compose UI with navigation
 */
class MainActivity : ComponentActivity() {

    /**
     * Permission launcher for requesting runtime permissions
     * Handles Android 13+ notification permission
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value

            // Log permission results (in production, you might want to show UI feedback)
            if (isGranted) {
                android.util.Log.d("MainActivity", "Permission granted: $permission")
            } else {
                android.util.Log.w("MainActivity", "Permission denied: $permission")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Request necessary permissions
        requestPermissions()

        // Set up Compose UI with navigation
        setContent {
            HoldOnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HoldOnNavigation()
                }
            }
        }
    }

    /**
     * Requests all necessary runtime permissions
     * Handles different permission requirements for different Android versions
     */
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Bluetooth permissions (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Request permissions if any are needed
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Handle returning to the app from other activities
     * Can be used to refresh UI state if needed
     */
    override fun onResume() {
        super.onResume()
        // Activity resumed - UI will automatically update through ViewModels
    }
}

/**
 * Navigation setup for the app
 * Defines all routes and screen transitions
 */
@Composable
fun HoldOnNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main screen route
        composable("main") {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Settings screen route
        composable("settings") {
            SettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}