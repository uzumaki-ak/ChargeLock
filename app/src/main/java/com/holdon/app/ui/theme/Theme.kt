/**
 * File: Theme.kt
 * Purpose: Main theme configuration for the app
 * Defines Material 3 theme with light and dark mode support
 */
package com.holdon.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme for Material 3
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Black,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = White,

    secondary = SecondaryLight,
    onSecondary = Black,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = White,

    tertiary = Success,
    onTertiary = White,

    error = ErrorLight,
    onError = Black,
    errorContainer = Error,
    onErrorContainer = White,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,

    outline = DividerDark,
    outlineVariant = DividerDark
)

/**
 * Light color scheme for Material 3
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,

    secondary = Secondary,
    onSecondary = White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryVariant,

    tertiary = Success,
    onTertiary = White,

    error = Error,
    onError = White,
    errorContainer = ErrorLight,
    onErrorContainer = Error,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight,

    outline = DividerLight,
    outlineVariant = DividerLight
)

/**
 * HoldOn app theme
 * Applies Material 3 theme with automatic light/dark mode switching
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content Composable content to apply theme to
 */
@Composable
fun HoldOnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}