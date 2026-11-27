package com.dexetra.securekeys.ui

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit

// Original SecureKeys palette (coral light / blue dark), darkened slightly where needed for contrast.
private val LightColors = lightColorScheme(
    primary = Color(0xFFD93B37),
    onPrimary = Color.White,
    background = Color(0xFFF2F2F2),
    onBackground = Color(0xFF353535),
    surface = Color(0xFFF2F2F2),
    onSurface = Color(0xFF353535),
    surfaceVariant = Color(0xFFEAEAEA),
    onSurfaceVariant = Color(0xFF616161),
    secondaryContainer = Color(0xFFDCDCDC),
    onSecondaryContainer = Color(0xFF353535),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0F90D7),
    onPrimary = Color(0xFF00131F),
    background = Color(0xFF222222),
    onBackground = Color(0xFFF1F1F1),
    surface = Color(0xFF222222),
    onSurface = Color(0xFFF1F1F1),
    surfaceVariant = Color(0xFF3E3E3E),
    onSurfaceVariant = Color(0xFFB8B8B8),
    secondaryContainer = Color(0xFF4A4A4A),
    onSecondaryContainer = Color(0xFFF1F1F1),
)

@Composable
fun SecureKeysTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}

/** Manual theme override; null follows the system. Same prefs file/key the legacy app used. */
class ThemePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    var dark: Boolean?
        get() = when (prefs.getString(KEY, null)) {
            "DARK" -> true
            "LIGHT" -> false
            else -> null
        }
        set(value) = prefs.edit { putString(KEY, if (value == true) "DARK" else "LIGHT") }

    private companion object {
        const val KEY = "current_theme"
    }
}
