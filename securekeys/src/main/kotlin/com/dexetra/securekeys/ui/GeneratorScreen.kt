package com.dexetra.securekeys.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.TextAutoSize
import com.dexetra.securekeys.GeneratorViewModel
import com.dexetra.securekeys.PasswordGenerator
import com.dexetra.securekeys.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    dark: Boolean,
    onHelp: () -> Unit,
    onToggleTheme: () -> Unit,
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
                title = { Text(stringResource(R.string.app_name)) },
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
                            text = { Text(stringResource(if (dark) R.string.theme_light else R.string.theme_dark)) },
                            onClick = { menuOpen = false; onToggleTheme() },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedSecureTextField(
                state = viewModel.passphrase,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_passphrase)) },
                textObfuscationMode = if (masked) TextObfuscationMode.RevealLastTyped else TextObfuscationMode.Visible,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                onKeyboardAction = { if (viewModel.canGenerate) generate() },
                trailingIcon = {
                    TextButton(onClick = { masked = !masked }) {
                        Text(stringResource(if (masked) R.string.show else R.string.hide))
                    }
                },
            )

            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.password_length), style = MaterialTheme.typography.titleMedium)
                    Text(
                        viewModel.length.value.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(state = viewModel.length, onValueChange = {}) // SliderState updates itself
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ParamTile("Aa", stringResource(R.string.param_mixed), viewModel.mixedCase, { viewModel.mixedCase = it }, Modifier.weight(1f))
                ParamTile("56", stringResource(R.string.param_numbers), viewModel.numbers, { viewModel.numbers = it }, Modifier.weight(1f))
                ParamTile("#*", stringResource(R.string.param_special), viewModel.special, { viewModel.special = it }, Modifier.weight(1f))
            }

            Button(
                onClick = ::generate,
                enabled = viewModel.canGenerate,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text(stringResource(R.string.generate), style = MaterialTheme.typography.titleMedium) }

            viewModel.password?.let { password ->
                Surface(
                    onClick = {
                        copyToClipboard(context, password)
                        viewModel.copied = true
                    },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.your_password), style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = password,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 32.sp),
                        )
                        Text(
                            stringResource(R.string.tap_to_copy),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamTile(
    symbol: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (checked) colors.primary else colors.surfaceVariant,
        contentColor = if (checked) colors.onPrimary else colors.onSurfaceVariant,
        modifier = modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
    ) {
        Column(
            modifier = Modifier.heightIn(min = 96.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(symbol, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clip = ClipData.newPlainText("secure", text)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Keeps the value out of clipboard previews; Android 13+ also shows its own copy confirmation.
        clip.description.extras = PersistableBundle().apply { putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true) }
    } else {
        Toast.makeText(context, R.string.password_copied, Toast.LENGTH_SHORT).show()
    }
    context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
}
