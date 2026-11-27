package xyz.mvij.securekeys.ui

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import xyz.mvij.securekeys.GeneratorViewModel
import xyz.mvij.securekeys.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: GeneratorViewModel,
    splashDone: () -> Boolean,
    onHelp: () -> Unit,
    onSettings: () -> Unit,
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val haptics = LocalHapticFeedback.current
    var masked by rememberSaveable { mutableStateOf(true) }
    var menuOpen by remember { mutableStateOf(false) }
    val generateInteraction = remember { MutableInteractionSource() }
    val generateScale = pressScale(generateInteraction)

    fun generate() {
        focus.clearFocus()
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        viewModel.generate()
    }

    // A light tick for each step of the length slider, like a detent.
    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.length.value.toInt() }
            .drop(1)
            .collect { haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { BrandMark(splashDone) },
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
                    LengthCounter(viewModel.length.value.toInt())
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
                    interactionSource = generateInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .graphicsLayer {
                            scaleX = generateScale.value
                            scaleY = generateScale.value
                        },
                ) { Text(stringResource(R.string.generate), style = MaterialTheme.typography.titleMedium) }
                Spacer(Modifier.height(36.dp))

                KeyResult(viewModel, onCopy = { copyToClipboard(context, it) })
            }
        }
    }
}

/** The length as a rolling counter: digits slide up as it grows and down as it shrinks. */
@Composable
private fun LengthCounter(length: Int) {
    AnimatedContent(
        targetState = length,
        transitionSpec = {
            val up = targetState > initialState
            val enter = slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { if (up) it / 2 else -it / 2 } + fadeIn(tween(120))
            val exit = slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { if (up) -it / 2 else it / 2 } + fadeOut(tween(90))
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        contentAlignment = Alignment.BottomEnd,
        label = "length",
    ) { value ->
        Text(value.toString(), style = MaterialTheme.typography.displaySmall)
    }
}

/** Scale for a control that dips slightly while pressed and springs back on release. */
@Composable
private fun pressScale(interactionSource: MutableInteractionSource, pressed: Float = 0.96f): State<Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    return animateFloatAsState(
        targetValue = if (isPressed) pressed else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
}

@Composable
private fun KeyResult(viewModel: GeneratorViewModel, onCopy: (String) -> Unit) {
    val password = viewModel.password
    val generation = viewModel.generation
    var justCopied by remember(generation) { mutableStateOf(false) }
    var copies by remember(generation) { mutableIntStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val copyLabel = stringResource(R.string.copy_password)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val keyModifier = if (password == null) {
            Modifier
        } else {
            Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClickLabel = copyLabel) {
                    onCopy(password)
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    viewModel.copied = true
                    justCopied = true
                    copies++
                }
        }
        KeyBlade(password, keyModifier, cutKey = generation, shineKey = copies, cutHaptics = true)
        val hint = when {
            password == null -> R.string.key_blank
            justCopied -> R.string.copied
            else -> R.string.tap_to_copy
        }
        AnimatedContent(
            targetState = hint,
            transitionSpec = {
                (fadeIn(tween(200, delayMillis = 60)) + slideInVertically { it / 3 }) togetherWith fadeOut(tween(100))
            },
            modifier = Modifier
                .padding(top = 12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            label = "hint",
        ) { res ->
            Text(
                text = stringResource(res),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * On: tonal fill with a teal edge. Off: just an outline, so the state never relies on colour alone.
 * The colours ease between states, the tile dips while pressed, and the symbol pops on each toggle.
 */
@Composable
private fun OptionToggle(
    symbol: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }
    val tileScale = pressScale(interaction, pressed = 0.94f)
    val pop = remember { Animatable(1f) }
    val container by animateColorAsState(if (checked) colors.primaryContainer else colors.background, label = "container")
    val content by animateColorAsState(if (checked) colors.onPrimaryContainer else colors.onSurfaceVariant, label = "content")
    val edge by animateColorAsState(if (checked) colors.primary else colors.outlineVariant, label = "edge")
    val edgeWidth by animateDpAsState(if (checked) 1.5.dp else 1.dp, label = "edgeWidth")

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        border = BorderStroke(edgeWidth, edge),
        modifier = modifier
            .graphicsLayer {
                scaleX = tileScale.value
                scaleY = tileScale.value
            }
            .clip(MaterialTheme.shapes.medium)
            .toggleable(
                value = checked,
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Checkbox,
                onValueChange = { on ->
                    haptics.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    scope.launch {
                        pop.snapTo(if (on) 0.6f else 0.85f)
                        pop.animateTo(1f, spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMediumLow))
                    }
                    onCheckedChange(on)
                },
            ),
    ) {
        Column(
            modifier = Modifier.heightIn(min = 76.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                symbol,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                    rotationZ = (1f - pop.value) * -40f
                },
            )
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
