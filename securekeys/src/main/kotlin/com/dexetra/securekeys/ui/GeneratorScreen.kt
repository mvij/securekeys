package com.dexetra.securekeys.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dexetra.securekeys.GeneratorViewModel
import com.dexetra.securekeys.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    onHelp: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var masked by rememberSaveable { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }

    fun generate() {
        focus.clearFocus()
        viewModel.generate()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.help)) },
                            onClick = { menuOpen = false; onHelp() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = { menuOpen = false; onSettings() },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                OutlinedSecureTextField(
                    state = viewModel.passphrase,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.enter_passphrase)) },
                    shape = MaterialTheme.shapes.medium,
                    textObfuscationMode = if (masked) TextObfuscationMode.RevealLastTyped else TextObfuscationMode.Visible,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    onKeyboardAction = { if (viewModel.canGenerate) generate() },
                    trailingIcon = {
                        TextButton(onClick = { masked = !masked }) {
                            Text(stringResource(if (masked) R.string.show else R.string.hide))
                        }
                    },
                )
                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.password_length),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(bottom = 4.dp),
                    )
                    Text(
                        viewModel.length.value.toInt().toString(),
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
                // Passing onValueChange makes the caller own updates: an empty lambda swallowed every drag.
                Slider(state = viewModel.length, onValueChange = { viewModel.length.value = it })
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionToggle("Aa", stringResource(R.string.param_mixed), viewModel.mixedCase, { viewModel.mixedCase = it }, Modifier.weight(1f))
                    OptionToggle("56", stringResource(R.string.param_numbers), viewModel.numbers, { viewModel.numbers = it }, Modifier.weight(1f))
                    OptionToggle("#*", stringResource(R.string.param_special), viewModel.special, { viewModel.special = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = ::generate,
                    enabled = viewModel.canGenerate,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) { Text(stringResource(R.string.generate), style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.height(36.dp))

                KeyResult(viewModel, onCopy = { copyToClipboard(context, it) })
            }
        }
    }
}

@Composable
private fun KeyResult(viewModel: GeneratorViewModel, onCopy: (String) -> Unit) {
    val password = viewModel.password
    var justCopied by remember(password) { mutableStateOf(false) }
    val copyLabel = stringResource(R.string.copy_password)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val keyModifier = if (password == null) {
            Modifier
        } else {
            Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClickLabel = copyLabel) {
                    onCopy(password)
                    viewModel.copied = true
                    justCopied = true
                }
        }
        KeyBlade(password, keyModifier)
        Text(
            text = stringResource(
                when {
                    password == null -> R.string.key_blank
                    justCopied -> R.string.copied
                    else -> R.string.tap_to_copy
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/** On: tonal fill with a teal edge. Off: just an outline, so the state never relies on colour alone. */
@Composable
private fun OptionToggle(
    symbol: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (checked) colors.primaryContainer else colors.background,
        contentColor = if (checked) colors.onPrimaryContainer else colors.onSurfaceVariant,
        border = if (checked) BorderStroke(1.5.dp, colors.primary) else BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
    ) {
        Column(
            modifier = Modifier.heightIn(min = 76.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(symbol, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clip = ClipData.newPlainText("secure", text)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Keeps the value out of clipboard previews. The line under the key confirms the copy.
        clip.description.extras = PersistableBundle().apply { putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true) }
    }
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
}
