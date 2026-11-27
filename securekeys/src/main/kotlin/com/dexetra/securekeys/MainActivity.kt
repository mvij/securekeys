package com.dexetra.securekeys

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
import com.dexetra.securekeys.ui.SecureKeysApp
import com.dexetra.securekeys.ui.SecureKeysTheme
import com.dexetra.securekeys.ui.ThemePrefs

class MainActivity : ComponentActivity() {
    private val viewModel: GeneratorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePrefs = ThemePrefs(this)
        setContent {
            var darkPref by remember { mutableStateOf(themePrefs.dark) }
            val dark = darkPref ?: isSystemInDarkTheme()

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
                    dark = dark,
                    onToggleTheme = {
                        darkPref = !dark
                        themePrefs.dark = !dark
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
}
