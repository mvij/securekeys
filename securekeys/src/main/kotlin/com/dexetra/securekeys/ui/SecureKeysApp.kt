package com.dexetra.securekeys.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.dexetra.securekeys.GeneratorViewModel

@Composable
fun SecureKeysApp(viewModel: GeneratorViewModel, dark: Boolean, onToggleTheme: () -> Unit) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    if (showHelp) {
        BackHandler { showHelp = false }
        HelpScreen(onBack = { showHelp = false })
    } else {
        GeneratorScreen(
            viewModel = viewModel,
            dark = dark,
            onHelp = { showHelp = true },
            onToggleTheme = onToggleTheme,
        )
    }
}
