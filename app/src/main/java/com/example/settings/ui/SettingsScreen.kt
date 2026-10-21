package com.example.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.AppSettings
import com.example.settings.GameSpeed
import com.example.settings.SettingsManager
import com.example.settings.TiltSensitivity
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Section: Motion Controls
        SettingsSection(title = "MOTION & TILT CONTROLS") {
            // Toggle Motion Controls
            SettingsSwitchRow(
                icon = Icons.Default.ScreenRotation,
                title = "Tilt Controls",
                subtitle = "Tilt phone to steer the red snake",
                checked = settings.motionControlsEnabled,
                onCheckedChange = { settingsManager.setMotionControlsEnabled(it) },
                tag = "switch_motion_controls"
            )

            HorizontalDivider(color = MidGrey, thickness = 0.5.dp)

            SettingsSwitchRow(
                icon = Icons.Default.SwapCalls,
                title = "Invert Tilt Direction",
                subtitle = "Reverse left/right and up/down tilt axes",
                checked = settings.invertTiltDirection,
                onCheckedChange = { settingsManager.setInvertTiltDirection(it) },
                tag = "switch_invert_tilt"
            )

            HorizontalDivider(color = MidGrey, thickness = 0.5.dp)

            // Tilt Sensitivity Selector
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Tilt Sensitivity",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Determines angle needed to change direction",
                    color = LightGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TiltSensitivity.values().forEach { sens ->
                        val isSelected = settings.tiltSensitivity == sens
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryYellow else MidGrey)
                                .clickable { settingsManager.setTiltSensitivity(sens) }
                                .padding(vertical = 10.dp)
                                .testTag("btn_sensitivity_${sens.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = sens.displayName,
                                    color = if (isSelected) DeepCharcoal else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "±${sens.thresholdDeg.toInt()}°",
                                    color = if (isSelected) DeepCharcoal.copy(alpha = 0.8f) else LightGrey,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Gameplay & Difficulty
        SettingsSection(title = "GAMEPLAY & SPEED") {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Snake Speed",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tick update interval for the snake movement",
                    color = LightGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameSpeed.values().forEach { speed ->
                        val isSelected = settings.gameSpeed == speed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryYellow else MidGrey)
                                .clickable { settingsManager.setGameSpeed(speed) }
                                .padding(vertical = 10.dp)
                                .testTag("btn_speed_${speed.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = speed.displayName,
                                color = if (isSelected) DeepCharcoal else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: Audio & Haptics
        SettingsSection(title = "FEEDBACK & SOUNDS") {
            SettingsSwitchRow(
                icon = Icons.Default.VolumeUp,
                title = "Sound Effects",
                subtitle = "Synthesized click, eat, and game audio",
                checked = settings.soundEnabled,
                onCheckedChange = { settingsManager.setSoundEnabled(it) },
                tag = "switch_sound_effects"
            )

            HorizontalDivider(color = MidGrey, thickness = 0.5.dp)

            SettingsSwitchRow(
                icon = Icons.Default.Vibration,
                title = "Vibration Feedback",
                subtitle = "Tactile haptic clicks and collision bumps",
                checked = settings.vibrationEnabled,
                onCheckedChange = { settingsManager.setVibrationEnabled(it) },
                tag = "switch_vibration"
            )
        }

        // Section: High Score Management
        SettingsSection(title = "GAME RECORDS") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current High Score",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${settings.highScore} points",
                        color = PrimaryYellow,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SnakeRed
                    ),
                    modifier = Modifier.testTag("btn_reset_high_score")
                ) {
                    Text("Reset Record")
                }
            }
        }

        // Tilt Instructions Info Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = DarkGrey
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "How Motion Controls Work",
                        color = PrimaryYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "• Hold your phone naturally and tap 'Calibrate' on the game screen to set your neutral resting angle.\n" +
                            "• Tilt left or right to steer the snake horizontally.\n" +
                            "• Tilt forward (away from you) to move up; tilt backward (toward you) to move down.\n" +
                            "• On-screen D-Pad is always available as a tactile backup.",
                    color = LightGrey,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(text = "Reset High Score?", color = Color.White)
            },
            text = {
                Text(
                    text = "Are you sure you want to reset your high score back to 0? This cannot be undone.",
                    color = LightGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsManager.resetHighScore()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnakeRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = LightGrey)
                }
            },
            containerColor = DeepCharcoal
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = PrimaryYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = DarkGrey
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryYellow,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = LightGrey,
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepCharcoal,
                checkedTrackColor = PrimaryYellow,
                uncheckedThumbColor = LightGrey,
                uncheckedTrackColor = MidGrey
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
