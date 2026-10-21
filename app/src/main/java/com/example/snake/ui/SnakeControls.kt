package com.example.snake.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake.model.GameStatus
import com.example.snake.model.SnakeDirection
import com.example.snake.model.SnakeGameState
import com.example.ui.theme.*

@Composable
fun SnakeScoreHeader(
    gameState: SnakeGameState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = DarkGrey
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Score
            Column {
                Text(
                    text = "SCORE",
                    color = LightGrey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${gameState.score}",
                    color = PrimaryYellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // High Score
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "High Score",
                        tint = SecondaryYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "HIGH SCORE",
                        color = LightGrey,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${gameState.highScore}",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SnakeActionButtons(
    status: GameStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (status) {
            GameStatus.IDLE -> {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DeepCharcoal
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_start_game")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START GAME", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
            GameStatus.RUNNING -> {
                Button(
                    onClick = onPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidGrey,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_pause_game")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PAUSE", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B1E1E),
                        contentColor = SnakeRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_restart_game")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTART", fontWeight = FontWeight.Bold)
                }
            }
            GameStatus.PAUSED -> {
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DeepCharcoal
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_resume_game")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESUME", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidGrey,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_restart_game")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESTART", fontWeight = FontWeight.Bold)
                }
            }
            GameStatus.GAME_OVER -> {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryYellow,
                        contentColor = DeepCharcoal
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_play_again")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun SnakeDPad(
    onDirection: (SnakeDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // UP
        DPadButton(
            icon = Icons.Default.KeyboardArrowUp,
            tag = "btn_dpad_up",
            onClick = { onDirection(SnakeDirection.UP) }
        )

        // LEFT, CENTER, RIGHT
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DPadButton(
                icon = Icons.Default.KeyboardArrowLeft,
                tag = "btn_dpad_left",
                onClick = { onDirection(SnakeDirection.LEFT) }
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(PrimaryYellow.copy(alpha = 0.3f))
            )
            DPadButton(
                icon = Icons.Default.KeyboardArrowRight,
                tag = "btn_dpad_right",
                onClick = { onDirection(SnakeDirection.RIGHT) }
            )
        }

        // DOWN
        DPadButton(
            icon = Icons.Default.KeyboardArrowDown,
            tag = "btn_dpad_down",
            onClick = { onDirection(SnakeDirection.DOWN) }
        )
    }
}

@Composable
private fun DPadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(DarkGrey)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tag,
            tint = PrimaryYellow,
            modifier = Modifier.size(28.dp)
        )
    }
}
