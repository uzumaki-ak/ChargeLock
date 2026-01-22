/**
 * File: ProtectionToggle.kt
 * Purpose: Large circular toggle button for protection on/off
 * The main hero component - one tap to protect your device
 */
package com.holdon.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.holdon.app.ui.theme.NotProtected
import com.holdon.app.ui.theme.Protected

/**
 * Large circular toggle for protection status
 * Animates between protected and unprotected states
 *
 * @param isProtected Current protection state
 * @param onToggle Callback when user taps toggle
 * @param modifier Modifier for customization
 */
@Composable
fun ProtectionToggle(
    isProtected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for pulsing effect when protected
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Color animation
    val animatedColorState = animateColorAsState(
        targetValue = if (isProtected) Protected else NotProtected,
        animationSpec = tween(300),
        label = "color"
    )
    val animatedColor = animatedColorState.value

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main toggle button
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(if (isProtected) scale else 1f)
                .background(
                    color = animatedColor.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable(
                    onClick = onToggle,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = animatedColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = if (isProtected) "Protected" else "Not Protected",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status text
        Text(
            text = if (isProtected) "PROTECTED" else "NOT PROTECTED",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = animatedColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Instruction text
        Text(
            text = if (isProtected) "Monitoring active" else "Tap to activate protection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
