package com.example.snake.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake.model.SnakeDirection
import com.example.snake.sensor.MotionTelemetry
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SnakeMotionIndicator(
    telemetry: MotionTelemetry,
    motionEnabled: Boolean,
    onCalibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = DarkGrey
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Bubble level visualizer & telemetry text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Circular Bubble Level
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DeepCharcoal)
                        .border(1.5.dp, if (motionEnabled) PrimaryYellow else LightGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val maxOffset = size.width * 0.35f

                        // Crosshair center
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = size.width * 0.15f,
                            center = Offset(centerX, centerY)
                        )

                        if (motionEnabled && telemetry.isSensorAvailable) {
                            // Bubble position based on delta roll and pitch
                            val bubbleX = (centerX + (telemetry.deltaRoll / 25f) * maxOffset).coerceIn(4f, size.width - 4f)
                            val bubbleY = (centerY - (telemetry.deltaPitch / 25f) * maxOffset).coerceIn(4f, size.height - 4f)

                            drawCircle(
                                color = SnakeRed,
                                radius = 5.dp.toPx(),
                                center = Offset(bubbleX, bubbleY)
                            )
                        } else {
                            // Centered dot
                            drawCircle(
                                color = LightGrey,
                                radius = 3.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                        }
                    }
                }

                // Status text and degrees
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (motionEnabled && telemetry.isSensorAvailable) AccentGreen else LightGrey)
                        )
                        Text(
                            text = if (!motionEnabled) "Touch / D-Pad Mode"
                            else if (!telemetry.isSensorAvailable) "Sensor Unavailable"
                            else "Tilt Steering Active",
                            color = if (motionEnabled && telemetry.isSensorAvailable) PrimaryYellow else LightGrey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (motionEnabled && telemetry.isSensorAvailable) {
                        Text(
                            text = "Tilt: X: ${telemetry.deltaRoll.roundToInt()}° | Y: ${telemetry.deltaPitch.roundToInt()}°",
                            color = LightGrey,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Use D-Pad below to steer",
                            color = LightGrey,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Right: Directional arrows feedback and Calibrate button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Active direction indicator arrows
                if (motionEnabled && telemetry.isSensorAvailable) {
                    DirectionalPill(activeDirection = telemetry.activeDirection)

                    // Calibrate Neutral Button
                    FilledTonalButton(
                        onClick = onCalibrate,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = PrimaryYellow.copy(alpha = 0.2f),
                            contentColor = PrimaryYellow
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_calibrate_tilt")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Calibrate Neutral",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (telemetry.isCalibrated) "Calibrated" else "Calibrate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionalPill(activeDirection: SnakeDirection?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (activeDirection != null) PrimaryYellow else MidGrey),
        contentAlignment = Alignment.Center
    ) {
        val icon = when (activeDirection) {
            SnakeDirection.UP -> Icons.Default.KeyboardArrowUp
            SnakeDirection.DOWN -> Icons.Default.KeyboardArrowDown
            SnakeDirection.LEFT -> Icons.Default.KeyboardArrowLeft
            SnakeDirection.RIGHT -> Icons.Default.KeyboardArrowRight
            null -> Icons.Default.ControlCamera
        }
        Icon(
            imageVector = icon,
            contentDescription = "Tilt Direction",
            tint = if (activeDirection != null) DeepCharcoal else LightGrey,
            modifier = Modifier.size(20.dp)
        )
    }
}
