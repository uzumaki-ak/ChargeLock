/**
 * File: AlarmScreen.kt
 * Purpose: Full-screen alarm display when triggered
 * Shows biometric prompt for dismissal
 */
package com.holdon.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.holdon.app.data.model.AlarmType
import com.holdon.app.ui.theme.AlarmRed
import com.holdon.app.util.BiometricHelper

/**
 * Full-screen alarm display
 * Requires biometric authentication to dismiss
 *
 * @param alarmType Type of alarm that was triggered
 * @param onDismissed Callback when alarm is successfully dismissed
 */
@Composable
fun AlarmScreen(
    alarmType: AlarmType,
    onDismissed: () -> Unit
) {
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }

    // Pulsing animation for alarm icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Flash animation for background
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Show biometric prompt on first composition
    LaunchedEffect(Unit) {
        if (context is FragmentActivity && biometricHelper.isBiometricAvailable()) {
            biometricHelper.showBiometricPrompt(
                activity = context,
                title = "Verify Identity",
                subtitle = "Authenticate to dismiss alarm",
                onSuccess = onDismissed,
                onError = { /* User can retry */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlarmRed.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Alarm icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alarm",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Alarm title
            Text(
                text = "SECURITY ALERT!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alarm reason
            Text(
                text = alarmType.getDisplayName(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Dismiss button
            Button(
                onClick = {
                    if (context is FragmentActivity) {
                        biometricHelper.showBiometricPrompt(
                            activity = context,
                            title = "Verify Identity",
                            subtitle = "Authenticate to dismiss alarm",
                            onSuccess = onDismissed,
                            onError = { /* User can retry */ }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AlarmRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "AUTHENTICATE TO DISMISS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = "Use fingerprint, face unlock, or PIN",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}