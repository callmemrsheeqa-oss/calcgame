package com.example.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake.model.SnakeDirection
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.LightGrey
import com.example.ui.theme.PrimaryYellow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    onDirectionChange: (SnakeDirection) -> Unit,
    modifier: Modifier = Modifier,
    baseSize: Int = 130,
    knobSize: Int = 54
) {
    val density = LocalDensity.current
    val baseSizeDp = baseSize.dp
    val knobSizeDp = knobSize.dp

    val baseRadiusPx = with(density) { (baseSizeDp / 2).toPx() }
    val maxDragPx = with(density) { ((baseSize - knobSize) / 2 + 8).dp.toPx() }
    val deadzonePx = with(density) { 10.dp.toPx() }

    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }
    var activeDirection by remember { mutableStateOf<SnakeDirection?>(null) }

    Box(
        modifier = modifier
            .size(baseSizeDp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(DarkGrey, DeepCharcoal)
                )
            )
            .border(2.5.dp, PrimaryYellow.copy(alpha = 0.85f), CircleShape)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    fun processOffset(currentTouch: Offset) {
                        val dx = currentTouch.x - baseRadiusPx
                        val dy = currentTouch.y - baseRadiusPx
                        val dist = sqrt(dx * dx + dy * dy)
                        val angle = atan2(dy, dx)
                        val clampedDist = dist.coerceAtMost(maxDragPx)
                        knobOffsetX = cos(angle) * clampedDist
                        knobOffsetY = sin(angle) * clampedDist

                        if (dist >= deadzonePx) {
                            val dir = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                            } else {
                                if (dy > 0) SnakeDirection.DOWN else SnakeDirection.UP
                            }
                            if (dir != activeDirection) {
                                activeDirection = dir
                                onDirectionChange(dir)
                            }
                        }
                    }

                    processOffset(down.position)

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change != null && change.pressed) {
                            change.consume()
                            processOffset(change.position)
                        } else {
                            break
                        }
                    }

                    // On release, snap back smoothly
                    knobOffsetX = 0f
                    knobOffsetY = 0f
                    activeDirection = null
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner Guide Rings & Crosshairs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(
                color = PrimaryYellow.copy(alpha = 0.2f),
                radius = size.width * 0.36f,
                center = center
            )
        }

        // Direction Arrow Labels
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "▲",
                color = if (activeDirection == SnakeDirection.UP) PrimaryYellow else LightGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
            )
            Text(
                text = "▼",
                color = if (activeDirection == SnakeDirection.DOWN) PrimaryYellow else LightGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
            )
            Text(
                text = "◄",
                color = if (activeDirection == SnakeDirection.LEFT) PrimaryYellow else LightGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 5.dp)
            )
            Text(
                text = "►",
                color = if (activeDirection == SnakeDirection.RIGHT) PrimaryYellow else LightGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 5.dp)
            )
        }

        // Thumbstick Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffsetX.roundToInt(), knobOffsetY.roundToInt()) }
                .size(knobSizeDp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryYellow, Color(0xFFD4AF37))
                    )
                )
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(DeepCharcoal)
            )
        }
    }
}
