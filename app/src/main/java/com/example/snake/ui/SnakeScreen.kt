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
import kotlin.math.sqrt

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
    val minSwipeDistancePx = with(density) { 16.dp.toPx() }

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

    // Full screen container with swipe & tap detection everywhere
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startPos = down.position
                    var registeredDirection = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (change.pressed) {
                            val currentPos = change.position
                            val dx = currentPos.x - startPos.x
                            val dy = currentPos.y - startPos.y
                            val dist = sqrt(dx * dx + dy * dy)

                            // Swipe / Drag detection in any direction
                            if (!registeredDirection && dist >= minSwipeDistancePx) {
                                registeredDirection = true
                                change.consume()

                                // If dragging up to down (dy > 0) -> DOWN
                                // If dragging down to up (dy < 0) -> UP
                                // If dragging left to right (dx > 0) -> RIGHT
                                // If dragging right to left (dx < 0) -> LEFT
                                val dir = if (abs(dx) > abs(dy)) {
                                    if (dx > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                } else {
                                    if (dy > 0) SnakeDirection.DOWN else SnakeDirection.UP
                                }
                                viewModel.queueDirection(dir)
                            }
                        } else {
                            // Touch released without major swipe: Treat as Click / Tap
                            if (!registeredDirection) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val dx = startPos.x - centerX
                                val dy = startPos.y - centerY

                                val dir = if (abs(dx) > abs(dy)) {
                                    if (dx > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
                                } else {
                                    if (dy > 0) SnakeDirection.DOWN else SnakeDirection.UP
                                }
                                viewModel.queueDirection(dir)
                            }
                            break
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_back_to_calculator")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Score
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkGrey,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SCORE: ", color = LightGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${state.score}", color = PrimaryYellow, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                            Text("|", color = MidGrey)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SecondaryYellow, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("BEST: ", color = LightGrey, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${state.highScore}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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

            // CENTER: EXPANSIVE BORDERLESS CONTINUOUS SNAKE BOARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SnakeBoard(
                    gameState = state,
                    onStartGame = { ensureLandscapeAndStart() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // BOTTOM HELPER HINT
            Text(
                text = "Swipe in any direction or click anywhere to steer • Snake wraps through edges",
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
