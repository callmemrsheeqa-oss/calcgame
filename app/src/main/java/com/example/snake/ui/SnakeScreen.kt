package com.example.snake.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.settings.AppSettings
import com.example.snake.SnakeViewModel
import com.example.snake.model.GameStatus
import com.example.snake.model.SnakeDirection
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun SnakeScreen(
    viewModel: SnakeViewModel,
    settings: AppSettings,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 14.dp.toPx() }

    var showJoystick by remember { mutableStateOf(false) }

    // Automatic Landscape on entering Snake Game, restore Portrait on exit
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            viewModel.pauseGame()
        }
    }

    fun ensureLandscapeAndStart() {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        viewModel.startGame()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP FLOATING HUD BAR: Back button, Scores, and Game Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to Calculator Button
                OutlinedButton(
                    onClick = onNavigateToCalculator,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryYellow),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_back_to_calculator")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calculator", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Scores & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Score
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkGrey,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SCORE: ", color = LightGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${state.score}", color = PrimaryYellow, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                            Text("|", color = MidGrey)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SecondaryYellow, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("BEST: ", color = LightGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${state.highScore}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Joystick Toggle Button
                    OutlinedButton(
                        onClick = { showJoystick = !showJoystick },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (showJoystick) PrimaryYellow else LightGrey
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("btn_toggle_joystick")
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showJoystick) "Hide Stick" else "Joystick", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Action Button
                    when (state.status) {
                        GameStatus.IDLE -> {
                            Button(
                                onClick = { ensureLandscapeAndStart() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DeepCharcoal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("btn_start_game")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("START", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        GameStatus.RUNNING -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.pauseGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MidGrey, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_pause_game")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PAUSE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.restartGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E1E), contentColor = SnakeRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_restart_game")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESTART", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                        GameStatus.PAUSED -> {
                            Button(
                                onClick = { viewModel.resumeGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DeepCharcoal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("btn_resume_game")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESUME", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        GameStatus.GAME_OVER -> {
                            Button(
                                onClick = { ensureLandscapeAndStart() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow, contentColor = DeepCharcoal),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("btn_play_again")
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PLAY AGAIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // CENTER: EXPANSIVE BORDERLESS CONTINUOUS SNAKE BOARD WITH FULL GESTURE CAPTURE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val startPos = down.position
                            var currentAccumX = 0f
                            var currentAccumY = 0f
                            var hasSwiped = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (change.pressed) {
                                    val delta = change.position - change.previousPosition
                                    currentAccumX += delta.x
                                    currentAccumY += delta.y

                                    val absAccumX = abs(currentAccumX)
                                    val absAccumY = abs(currentAccumY)

                                    if (absAccumX >= swipeThresholdPx || absAccumY >= swipeThresholdPx) {
                                        hasSwiped = true
                                        change.consume()
                                        val dir = if (absAccumX > absAccumY) {
                                            if (currentAccumX > 0f) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                        } else {
                                            if (currentAccumY > 0f) SnakeDirection.DOWN else SnakeDirection.UP
                                        }
                                        viewModel.queueDirection(dir, fromTouch = true)
                                        currentAccumX = 0f
                                        currentAccumY = 0f
                                    }
                                } else {
                                    // Pointer released
                                    if (!hasSwiped) {
                                        if (state.status == GameStatus.IDLE) {
                                            viewModel.startGame()
                                        } else if (state.status == GameStatus.PAUSED) {
                                            viewModel.resumeGame()
                                        } else if (state.status == GameStatus.RUNNING) {
                                            val centerX = size.width / 2f
                                            val centerY = size.height / 2f
                                            val dx = startPos.x - centerX
                                            val dy = startPos.y - centerY
                                            val dir = if (abs(dx) > abs(dy)) {
                                                if (dx > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                            } else {
                                                if (dy > 0) SnakeDirection.DOWN else SnakeDirection.UP
                                            }
                                            viewModel.queueDirection(dir, fromTouch = true)
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                SnakeBoard(
                    gameState = state,
                    onStartGame = { ensureLandscapeAndStart() },
                    modifier = Modifier.fillMaxSize()
                )

                // Optional floating Virtual Joystick on the right side
                if (showJoystick) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 8.dp)
                    ) {
                        VirtualJoystick(
                            onDirectionChange = { dir ->
                                viewModel.queueDirection(dir, fromTouch = true)
                            },
                            baseSize = 110,
                            knobSize = 46
                        )
                    }
                }
            }

            // BOTTOM HELPER HINT
            Text(
                text = "Swipe anywhere on screen to steer • Snake wraps across edges",
                color = LightGrey.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }

    // Game Over Overlay
    if (state.status == GameStatus.GAME_OVER) {
        SnakeGameOverDialog(
            gameState = state,
            onPlayAgain = { ensureLandscapeAndStart() },
            onDismiss = { /* keep dialog */ }
        )
    }
}
