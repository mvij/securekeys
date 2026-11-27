package xyz.mvij.securekeys.ui

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import xyz.mvij.securekeys.R

/*
 * Brand: a key-cutting bench. Enamel teal for controls, brass for the cut key (the result),
 * on brushed nickel by day and blued steel by night.
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5D55),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE6E0),
    onPrimaryContainer = Color(0xFF08332F),
    secondaryContainer = Color(0xFFC9D3D2),
    onSecondaryContainer = Color(0xFF1C2A2E),
    background = Color(0xFFE4E8E7),
    onBackground = Color(0xFF1C2A2E),
    surface = Color(0xFFE4E8E7),
    onSurface = Color(0xFF1C2A2E),
    surfaceVariant = Color(0xFFD5DCDB),
    onSurfaceVariant = Color(0xFF56666A),
    surfaceContainerLowest = Color(0xFFF4F6F6),
    surfaceContainerLow = Color(0xFFEDF0EF),
    surfaceContainer = Color(0xFFEAEEED),
    surfaceContainerHigh = Color(0xFFE0E5E4),
    surfaceContainerHighest = Color(0xFFD9DFDE),
    outline = Color(0xFF7D8C8D),
    outlineVariant = Color(0xFFB9C3C2),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF62C6B5),
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF1E433E),
    onPrimaryContainer = Color(0xFFBDEDE3),
    secondaryContainer = Color(0xFF2A3B45),
    onSecondaryContainer = Color(0xFFE6ECEC),
    background = Color(0xFF13202A),
    onBackground = Color(0xFFE6ECEC),
    surface = Color(0xFF13202A),
    onSurface = Color(0xFFE6ECEC),
    surfaceVariant = Color(0xFF1F303A),
    onSurfaceVariant = Color(0xFF97A7AC),
    surfaceContainerLowest = Color(0xFF0E1921),
    surfaceContainerLow = Color(0xFF172530),
    surfaceContainer = Color(0xFF1B2A34),
    surfaceContainerHigh = Color(0xFF22323D),
    surfaceContainerHighest = Color(0xFF2A3B46),
    outline = Color(0xFF6B7D84),
    outlineVariant = Color(0xFF33444E),
)

/** Colours for the drawn key, which sits outside the Material roles. */
@Immutable
data class KeyColors(
    val metal: Color,
    val edge: Color,
    val ink: Color,
    val highlight: Color,
    val blank: Color,
)

private val LightKey = KeyColors(
    metal = Color(0xFFCB9A35),
    edge = Color(0xFF9C7322),
    ink = Color(0xFF3A2A08),
    highlight = Color(0x8CFFF0C8),
    blank = Color(0xFF9AA8A8),
)

private val DarkKey = KeyColors(
    metal = Color(0xFFD8AB4C),
    edge = Color(0xFFA57D2C),
    ink = Color(0xFF2E2106),
    highlight = Color(0x73FFECBE),
    blank = Color(0xFF4A5D68),
)

val LocalKeyColors = staticCompositionLocalOf { LightKey }

@OptIn(ExperimentalTextApi::class)
private fun archivo(weight: Int, width: Float = 100f) = Font(
    R.font.archivo,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width)),
)

/** Archivo at normal width: all running UI text. */
val Archivo = FontFamily(archivo(400), archivo(500), archivo(600), archivo(700))

/** Archivo at its widest: the wordmark, headings and numerals, like letters stamped into a key blank. */
val ArchivoExpanded = FontFamily(archivo(700, 125f), archivo(800, 125f))

/** Martian Mono, slightly condensed: only for the generated password, where every glyph must be unambiguous. */
@OptIn(ExperimentalTextApi::class)
val KeyMono = FontFamily(
    Font(
        R.font.martian_mono,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(620), FontVariation.width(87.5f)),
    ),
)

// Scale: 12 / 14 / 16 / 20 / 24 / 32, body set at 16 on 24.
private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = ArchivoExpanded, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = ArchivoExpanded, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontFamily = ArchivoExpanded, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

// Small radii for inputs and toggles, larger for the one primary action.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun SecureKeysTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalKeyColors provides if (dark) DarkKey else LightKey) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Theme choice from Settings. System follows the device; Light and Dark override it. */
enum class ThemeMode {
    System, Light, Dark;

    /** Whether to draw dark, given what the system currently asks for. */
    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }

    /** Value stored under the legacy key; null means no override (key removed). */
    val prefValue: String?
        get() = when (this) {
            System -> null
            Light -> "LIGHT"
            Dark -> "DARK"
        }

    companion object {
        fun fromPref(value: String?): ThemeMode = when (value) {
            "DARK" -> Dark
            "LIGHT" -> Light
            else -> System
        }
    }
}

/** Persisted theme choice, in the legacy app's prefs file/key layout (`<package>_preferences`, `current_theme`). */
class ThemePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = ThemeMode.fromPref(prefs.getString(KEY, null))
        set(value) = prefs.edit {
            val stored = value.prefValue
            if (stored == null) remove(KEY) else putString(KEY, stored)
        }

    private companion object {
        const val KEY = "current_theme"
    }
}
