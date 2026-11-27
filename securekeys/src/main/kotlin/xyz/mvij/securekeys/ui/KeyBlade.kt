package xyz.mvij.securekeys.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

// Key geometry in dp. The text overlay uses the same numbers so it lands on the flat of the blade.
private val KeyHeight = 96.dp
private val BowRadius = 34.dp
private val HoleRadius = 10.dp
private val BladeHeight = 58.dp
private val ShoulderWidth = 12.dp
private val ShoulderOverhang = 6.dp
private val TipLength = 18.dp
private val MaxCut = 18.dp
private val MinCut = 4.dp
private val ShoulderStart = BowRadius * 2 - 6.dp
private val ShoulderEnd = ShoulderStart + ShoulderWidth
private val BladeTop = (KeyHeight - BladeHeight) / 2
private val GrooveY = BladeTop + BladeHeight - MaxCut - 3.dp

/** Cut depth level (0..4) for each character, so a password always cuts the same key. */
internal fun bitting(password: String): List<Int> = password.map { (it.code * 31) % 5 }

/** Glyphs flicked through on uncut positions while the stamp settles. Display only. */
private const val ScramblePool = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789#*%&@$!?"

/** How many times the scrambled glyphs change over the whole cut. */
private const val ScrambleSteps = 24

/**
 * The stamp mid-cut: characters whose cut is made show for real, the rest flicker through
 * [ScramblePool]. Monospaced, so the line never changes width while it settles.
 */
internal fun stampText(password: String, progress: Float): String {
    if (progress >= 1f) return password
    val settled = (progress * password.length).toInt()
    val frame = (progress * ScrambleSteps).toInt()
    return buildString(password.length) {
        password.forEachIndexed { i, c ->
            append(if (i < settled) c else ScramblePool[Math.floorMod(i * 7919 + frame * 104729 + c.code, ScramblePool.length)])
        }
    }
}

/**
 * The generated password drawn as a brass key: its characters set the cuts along the blade and the
 * password is stamped on the flat. With no password it shows an uncut outline as a placeholder.
 *
 * Whenever [cutKey] changes the key slides in and cuts from the shoulder to the tip; each character
 * of the stamp settles as its cut is made, with a haptic tick per cut if [cutHaptics] is set. Then a
 * glint sweeps along the brass, and again whenever [shineKey] changes (e.g. on copy).
 */
@Composable
fun KeyBlade(
    password: String?,
    modifier: Modifier = Modifier,
    cutKey: Any? = password,
    shineKey: Int = 0,
    cutHaptics: Boolean = false,
) {
    val colors = LocalKeyColors.current
    val haptics = LocalHapticFeedback.current
    val cut = remember(cutKey) { Animatable(if (password == null) 1f else 0f) }
    val slide = remember(cutKey) { Animatable(if (password == null) 0f else 1f) }
    val shine = remember { Animatable(1f) }
    val levels = remember(password) { password?.let(::bitting).orEmpty() }

    LaunchedEffect(cutKey) {
        if (password == null) return@LaunchedEffect
        launch { slide.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) }
        if (cutHaptics) {
            launch {
                snapshotFlow { (cut.value * levels.size).toInt() }
                    .distinctUntilChanged()
                    .drop(1)
                    .collect { haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
            }
        }
        cut.animateTo(1f, tween(durationMillis = 700, easing = FastOutSlowInEasing))
        shine.snapTo(0f)
        shine.animateTo(1f, tween(durationMillis = 650, easing = LinearOutSlowInEasing))
    }
    LaunchedEffect(shineKey) {
        if (shineKey == 0 || password == null) return@LaunchedEffect
        shine.snapTo(0f)
        shine.animateTo(1f, tween(durationMillis = 550, easing = LinearOutSlowInEasing))
    }
    val stampOffset = with(LocalDensity.current) { 1.dp.toPx() }

    Box(
        modifier
            .fillMaxWidth()
            .height(KeyHeight)
            .graphicsLayer { translationX = slide.value * -24.dp.toPx() },
    ) {
        Canvas(Modifier.fillMaxWidth().height(KeyHeight)) {
            val (outline, hole) = keyPaths(levels, cut.value)
            if (password == null) {
                val dashed = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())))
                drawPath(outline, colors.blank, style = dashed)
                drawPath(hole, colors.blank, style = dashed)
            } else {
                drawPath(outline, colors.metal)
                if (shine.value < 1f) {
                    // A diagonal band of light travelling bow to tip, clipped to the key by drawing the outline.
                    val band = size.width * 0.18f
                    val x = -band + (size.width + 2 * band) * shine.value
                    drawPath(
                        outline,
                        Brush.linearGradient(
                            0f to Color.Transparent,
                            0.5f to colors.highlight,
                            1f to Color.Transparent,
                            start = Offset(x - band, 0f),
                            end = Offset(x + band, size.height),
                        ),
                    )
                }
                drawPath(outline, colors.edge, style = Stroke(width = 1.dp.toPx()))
                // Milled groove separating the stamped flat from the cuts.
                drawLine(
                    colors.edge,
                    Offset(ShoulderEnd.toPx() + 4.dp.toPx(), GrooveY.toPx()),
                    Offset(size.width - TipLength.toPx() - 4.dp.toPx(), GrooveY.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
        if (password != null) {
            Box(
                modifier = Modifier
                    .padding(start = ShoulderEnd + 10.dp, end = TipLength + 8.dp, top = BladeTop)
                    .height(GrooveY - BladeTop)
                    .graphicsLayer { alpha = (cut.value / 0.15f).coerceIn(0f, 1f) },
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = stampText(password, cut.value),
                    // Screen readers get the real password, never the scramble.
                    modifier = Modifier.clearAndSetSemantics { text = AnnotatedString(password) },
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 24.sp, stepSize = 0.5.sp),
                    style = TextStyle(
                        fontFamily = KeyMono,
                        color = colors.ink,
                        // A light edge below each glyph reads as letters pressed into the metal.
                        shadow = Shadow(color = colors.highlight, offset = Offset(0f, stampOffset), blurRadius = 0f),
                    ),
                )
            }
        }
    }
}

/** Returns the key outline (bow, shoulder, bitted blade, with the hole cut out) and the hole alone. */
private fun DrawScope.keyPaths(levels: List<Int>, progress: Float): Pair<Path, Path> {
    val w = size.width
    val cy = KeyHeight.toPx() / 2
    val r = BowRadius.toPx()
    val top = BladeTop.toPx()
    val bottom = top + BladeHeight.toPx()
    val sh0 = ShoulderStart.toPx()
    val sh1 = ShoulderEnd.toPx()
    val bladeEnd = w - TipLength.toPx()

    val bow = Path().apply { addOval(Rect(Offset(r + 1f, cy), r)) }
    val shoulder = Path().apply {
        val o = ShoulderOverhang.toPx()
        addRect(Rect(sh0, top - o, sh1, bottom + o))
    }

    val blade = Path().apply {
        moveTo(sh1 - 1f, top)
        lineTo(bladeEnd, top)
        lineTo(w - 1f, top + 12.dp.toPx())
        lineTo(bladeEnd - 6.dp.toPx(), bottom)
        // Bottom edge, right to left, with one flat-bottomed cut per character.
        val x0 = sh1 + 6.dp.toPx()
        val x1 = bladeEnd - 6.dp.toPx()
        lineTo(x1, bottom)
        if (levels.isNotEmpty()) {
            val step = (x1 - x0) / levels.size
            val minCut = MinCut.toPx()
            val range = MaxCut.toPx() - minCut
            for (i in levels.indices.reversed()) {
                val reveal = (progress * levels.size - i).coerceIn(0f, 1f)
                val depth = (minCut + range * levels[i] / 4f) * reveal
                val left = x0 + i * step
                lineTo(left + step * 0.7f, bottom - depth)
                lineTo(left + step * 0.3f, bottom - depth)
            }
        }
        lineTo(x0, bottom)
        lineTo(sh1 - 1f, bottom)
        close()
    }

    val hole = Path().apply { addOval(Rect(Offset(r + 1f - 8.dp.toPx(), cy), HoleRadius.toPx())) }
    val solid = Path.combine(PathOperation.Union, Path.combine(PathOperation.Union, bow, shoulder), blade)
    return Path.combine(PathOperation.Difference, solid, hole) to hole
}
