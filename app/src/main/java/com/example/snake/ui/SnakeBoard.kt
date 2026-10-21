package com.example.snake.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snake.model.GameStatus
import com.example.snake.model.GridPoint
import com.example.snake.model.SnakeDirection
import com.example.snake.model.SnakeGameState
import com.example.ui.theme.*

@Composable
fun SnakeBoard(
    gameState: SnakeGameState,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val targetAspect = gameState.gridWidth.toFloat() / gameState.gridHeight.toFloat()
        val availW = maxWidth
        val availH = maxHeight

        val (boardW, boardH) = if (availW / availH > targetAspect) {
            Pair(availH * targetAspect, availH)
        } else {
            Pair(availW, availW / targetAspect)
        }

        Box(
            modifier = Modifier
                .size(boardW, boardH)
                .clip(RoundedCornerShape(16.dp))
                .background(DeepCharcoal),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellWidth = size.width / gameState.gridWidth
                val cellHeight = size.height / gameState.gridHeight

                // Draw sleek grid pattern
                drawGridPattern(gameState.gridWidth, gameState.gridHeight, cellWidth, cellHeight)

                // Draw Food
                drawFood(gameState.food.point, gameState.food.isBonus, cellWidth, cellHeight)

                // Draw Continuous Snake Body & Head
                if (gameState.snake.isNotEmpty()) {
                    drawContinuousSnake(gameState.snake, gameState.direction, cellWidth, cellHeight)
                }
            }

            // Interactive Start / Resume Banner
            if (gameState.status == GameStatus.IDLE) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepCharcoal.copy(alpha = 0.9f),
                    border = BorderStroke(2.dp, PrimaryYellow),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryYellow, modifier = Modifier.size(24.dp))
                        Text(
                            text = "TAP TO START",
                            color = PrimaryYellow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else if (gameState.status == GameStatus.PAUSED) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DeepCharcoal.copy(alpha = 0.9f),
                    border = BorderStroke(2.dp, SecondaryYellow),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = SecondaryYellow, modifier = Modifier.size(20.dp))
                        Text(
                            text = "PAUSED - TAP TO RESUME",
                            color = SecondaryYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawGridPattern(cols: Int, rows: Int, cellW: Float, cellH: Float) {
    val lineCol = Color(0xFF222222)
    for (x in 1 until cols) {
        drawLine(
            color = lineCol,
            start = Offset(x * cellW, 0f),
            end = Offset(x * cellW, size.height),
            strokeWidth = 1f
        )
    }
    for (y in 1 until rows) {
        drawLine(
            color = lineCol,
            start = Offset(0f, y * cellH),
            end = Offset(size.width, y * cellH),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawContinuousSnake(
    snake: List<GridPoint>,
    direction: SnakeDirection,
    cellW: Float,
    cellH: Float
) {
    val centers = snake.map { p ->
        Offset(p.x * cellW + cellW / 2f, p.y * cellH + cellH / 2f)
    }

    val bodyRadius = (cellW.coerceAtMost(cellH)) * 0.42f
    val bodyStrokeWidth = bodyRadius * 2f

    // Continuous body path with rounded joints, splitting across screen wrap-arounds
    if (centers.size >= 2) {
        val paths = mutableListOf<Path>()
        var currentPath = Path()
        currentPath.moveTo(centers.first().x, centers.first().y)
        paths.add(currentPath)

        for (i in 1 until centers.size) {
            val prev = snake[i - 1]
            val curr = snake[i]
            val isWrapped = kotlin.math.abs(prev.x - curr.x) > 1 || kotlin.math.abs(prev.y - curr.y) > 1
            if (isWrapped) {
                currentPath = Path()
                currentPath.moveTo(centers[i].x, centers[i].y)
                paths.add(currentPath)
            } else {
                currentPath.lineTo(centers[i].x, centers[i].y)
            }
        }

        for (path in paths) {
            // Deep red shadow stroke
            drawPath(
                path = path,
                color = Color(0xFFB01010),
                style = Stroke(
                    width = bodyStrokeWidth + 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Main continuous red body
            drawPath(
                path = path,
                color = SnakeRed,
                style = Stroke(
                    width = bodyStrokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Continuous cylindrical spine highlight
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.28f),
                style = Stroke(
                    width = bodyStrokeWidth * 0.3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Segment joints
        for (i in 1 until centers.size) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.12f),
                radius = bodyRadius * 0.75f,
                center = centers[i]
            )
        }
    } else if (centers.size == 1) {
        drawCircle(
            color = SnakeRed,
            radius = bodyRadius,
            center = centers.first()
        )
    }

    // Snake Head
    val headCenter = centers.first()
    drawSnakeHead(headCenter, direction, cellW, cellH, bodyRadius)
}

private fun DrawScope.drawSnakeHead(
    center: Offset,
    direction: SnakeDirection,
    cellW: Float,
    cellH: Float,
    bodyRadius: Float
) {
    val headRadius = bodyRadius * 1.15f

    // Forked Tongue
    val tongueColor = Color(0xFFFF5252)
    val tongueLen = headRadius * 0.85f
    val tongueBase = when (direction) {
        SnakeDirection.UP -> Offset(center.x, center.y - headRadius)
        SnakeDirection.DOWN -> Offset(center.x, center.y + headRadius)
        SnakeDirection.LEFT -> Offset(center.x - headRadius, center.y)
        SnakeDirection.RIGHT -> Offset(center.x + headRadius, center.y)
    }
    val tongueTip = when (direction) {
        SnakeDirection.UP -> Offset(center.x, center.y - headRadius - tongueLen)
        SnakeDirection.DOWN -> Offset(center.x, center.y + headRadius + tongueLen)
        SnakeDirection.LEFT -> Offset(center.x - headRadius - tongueLen, center.y)
        SnakeDirection.RIGHT -> Offset(center.x + headRadius + tongueLen, center.y)
    }

    drawLine(
        color = tongueColor,
        start = tongueBase,
        end = tongueTip,
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Snake Head Circle
    drawCircle(
        color = SnakeHeadRed,
        radius = headRadius,
        center = center
    )

    // Inner Head Highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.18f),
        radius = headRadius * 0.5f,
        center = Offset(center.x, center.y - headRadius * 0.1f)
    )

    // Eyes
    val eyeRadius = headRadius * 0.32f
    val pupilRadius = eyeRadius * 0.5f
    val eyeOffsetDist = headRadius * 0.55f
    val eyeForwardDist = headRadius * 0.35f

    val (eye1, eye2) = when (direction) {
        SnakeDirection.UP -> Pair(
            Offset(center.x - eyeOffsetDist, center.y - eyeForwardDist),
            Offset(center.x + eyeOffsetDist, center.y - eyeForwardDist)
        )
        SnakeDirection.DOWN -> Pair(
            Offset(center.x - eyeOffsetDist, center.y + eyeForwardDist),
            Offset(center.x + eyeOffsetDist, center.y + eyeForwardDist)
        )
        SnakeDirection.LEFT -> Pair(
            Offset(center.x - eyeForwardDist, center.y - eyeOffsetDist),
            Offset(center.x - eyeForwardDist, center.y + eyeOffsetDist)
        )
        SnakeDirection.RIGHT -> Pair(
            Offset(center.x + eyeForwardDist, center.y - eyeOffsetDist),
            Offset(center.x + eyeForwardDist, center.y + eyeOffsetDist)
        )
    }

    drawCircle(color = SnakeEyeYellow, radius = eyeRadius, center = eye1)
    drawCircle(color = SnakeEyeYellow, radius = eyeRadius, center = eye2)

    val pupilShift = when (direction) {
        SnakeDirection.UP -> Offset(0f, -pupilRadius * 0.4f)
        SnakeDirection.DOWN -> Offset(0f, pupilRadius * 0.4f)
        SnakeDirection.LEFT -> Offset(-pupilRadius * 0.4f, 0f)
        SnakeDirection.RIGHT -> Offset(pupilRadius * 0.4f, 0f)
    }
    drawCircle(color = DeepCharcoal, radius = pupilRadius, center = eye1 + pupilShift)
    drawCircle(color = DeepCharcoal, radius = pupilRadius, center = eye2 + pupilShift)

    drawCircle(color = Color.White, radius = pupilRadius * 0.4f, center = eye1 + pupilShift - Offset(1f, 1f))
    drawCircle(color = Color.White, radius = pupilRadius * 0.4f, center = eye2 + pupilShift - Offset(1f, 1f))
}

private fun DrawScope.drawFood(point: GridPoint, isBonus: Boolean, cellW: Float, cellH: Float) {
    val centerX = point.x * cellW + cellW / 2
    val centerY = point.y * cellH + cellH / 2
    val radius = cellW * 0.4f

    if (isBonus) {
        drawCircle(
            color = GoldenApple.copy(alpha = 0.35f),
            radius = radius * 1.5f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = GoldenApple,
            radius = radius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.35f,
            center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f)
        )
    } else {
        drawCircle(
            color = FoodPurple,
            radius = radius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = AccentGreen,
            radius = radius * 0.35f,
            center = Offset(centerX, centerY - radius * 0.85f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = radius * 0.28f,
            center = Offset(centerX - radius * 0.3f, centerY - radius * 0.3f)
        )
    }
}
