package xyz.mvij.securekeys.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import xyz.mvij.securekeys.R

/** How long to wait for the splash to clear before playing the intro anyway. */
private const val IntroFallbackMs = 2_000L

/**
 * The top-bar logo: the launcher's brass key next to the wordmark. Once per launch, after the
 * splash clears, the key swings into place and the wordmark slides out from behind it. Tapping
 * the key gives it a little turn, like trying it in a lock.
 */
@Composable
fun BrandMark(splashDone: () -> Boolean, modifier: Modifier = Modifier) {
    var played by rememberSaveable { mutableStateOf(false) }
    val reveal = remember { Animatable(if (played) 1f else 0f) }
    val turn = remember { Animatable(if (played) 0f else -110f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        if (played) return@LaunchedEffect
        withTimeoutOrNull(IntroFallbackMs) { snapshotFlow(splashDone).first { it } }
        launch { turn.animateTo(0f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow)) }
        reveal.animateTo(1f, tween(durationMillis = 650, delayMillis = 120, easing = FastOutSlowInEasing))
        played = true
    }

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    rotationZ = turn.value
                    val grow = (1f + turn.value / 110f).coerceIn(0f, 1f) // 0 at the start, 1 once upright
                    scaleX = 0.6f + 0.4f * grow
                    scaleY = scaleX
                }
                // A plain gesture, not a clickable: it's a flourish, so screen readers shouldn't stop on it.
                .pointerInput(Unit) {
                    detectTapGestures {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        scope.launch {
                            turn.animateTo(32f, tween(durationMillis = 110))
                            turn.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                },
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            modifier = Modifier
                .graphicsLayer { translationX = (1f - reveal.value) * -16.dp.toPx() }
                // Wipes in from the key's side, so the letters seem to come out from behind it.
                .drawWithContent {
                    clipRect(right = size.width * reveal.value) { this@drawWithContent.drawContent() }
                },
        )
    }
}
