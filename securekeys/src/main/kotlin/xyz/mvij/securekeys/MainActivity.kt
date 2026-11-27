package xyz.mvij.securekeys

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import xyz.mvij.securekeys.ui.SecureKeysApp
import xyz.mvij.securekeys.ui.SecureKeysTheme
import xyz.mvij.securekeys.ui.ThemeMode
import xyz.mvij.securekeys.ui.ThemePrefs

class MainActivity : ComponentActivity() {
    private val viewModel: GeneratorViewModel by viewModels()

    /** Becomes true once the splash has cleared, so the top-bar brand mark animates in view. */
    private var splashDone by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        // No splash runs when the activity is recreated (rotation), so the intro counts as played.
        splashDone = savedInstanceState != null
        // Must run before super.onCreate: shows the brass-key splash, then swaps to Theme.SecureKeys.
        installSplashScreen().setOnExitAnimationListener { splash ->
            // Let the key finish turning (Android 12+; both values are 0 on older versions), then it
            // swells and fades as the splash clears.
            val turnEnd = splash.iconAnimationStartMillis + splash.iconAnimationDurationMillis
            val wait = (turnEnd - SystemClock.uptimeMillis()).coerceIn(0L, SPLASH_MAX_WAIT_MS)
            val easing = AccelerateInterpolator()
            splash.iconView.animate()
                .setStartDelay(wait)
                .scaleX(SPLASH_EXIT_SCALE)
                .scaleY(SPLASH_EXIT_SCALE)
                .setInterpolator(easing)
                .setDuration(SPLASH_EXIT_MS)
                .start()
            splash.view.animate()
                .setStartDelay(wait)
                .alpha(0f)
                .setInterpolator(easing)
                .setDuration(SPLASH_EXIT_MS)
                .withEndAction {
                    splash.remove()
                    splashDone = true
                }
                .start()
        }
        super.onCreate(savedInstanceState)
        val themePrefs = ThemePrefs(this)
        setContent {
            var themeMode by remember { mutableStateOf(themePrefs.mode) }
            val dark = themeMode.isDark(isSystemInDarkTheme())

            DisposableEffect(dark) {
                val bars = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                onDispose {}
            }

            SecureKeysTheme(dark) {
                SecureKeysApp(
                    viewModel = viewModel,
                    splashDone = { splashDone },
                    themeMode = themeMode,
                    onThemeModeChange = { mode: ThemeMode ->
                        themeMode = mode
                        themePrefs.mode = mode
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        // Don't leave a generated password on the clipboard once the app is closed (not on rotation).
        if (isFinishing && viewModel.copied) {
            getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("", ""))
        }
        super.onDestroy()
    }

    private companion object {
        const val SPLASH_EXIT_MS = 260L
        const val SPLASH_EXIT_SCALE = 1.35f

        /** Upper bound on waiting for the icon animation, so a slow device never holds the app back. */
        const val SPLASH_MAX_WAIT_MS = 1_000L
    }
}
