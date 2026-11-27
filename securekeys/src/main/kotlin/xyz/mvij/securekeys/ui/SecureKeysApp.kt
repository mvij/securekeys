package xyz.mvij.securekeys.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import xyz.mvij.securekeys.GeneratorViewModel

private enum class Screen { Generator, Help, Settings }

@Composable
fun SecureKeysApp(
    viewModel: GeneratorViewModel,
    splashDone: () -> Boolean,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(Screen.Generator) }
    val back = { screen = Screen.Generator }
    when (screen) {
        Screen.Generator -> GeneratorScreen(
            viewModel = viewModel,
            splashDone = splashDone,
            onHelp = { screen = Screen.Help },
            onSettings = { screen = Screen.Settings },
        )
        Screen.Help -> {
            BackHandler(onBack = back)
            HelpScreen(onBack = back)
        }
        Screen.Settings -> {
            BackHandler(onBack = back)
            SettingsScreen(themeMode = themeMode, onThemeModeChange = onThemeModeChange, onBack = back)
        }
    }
}
