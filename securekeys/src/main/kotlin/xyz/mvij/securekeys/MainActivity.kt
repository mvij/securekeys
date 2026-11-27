package xyz.mvij.securekeys

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: shows the brass-key splash, then swaps to Theme.SecureKeys.
        installSplashScreen().setOnExitAnimationListener { splash ->
            splash.view.animate()
                .alpha(0f)
                .setDuration(SPLASH_FADE_MS)
                .withEndAction { splash.remove() }
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
        const val SPLASH_FADE_MS = 200L
    }
}
