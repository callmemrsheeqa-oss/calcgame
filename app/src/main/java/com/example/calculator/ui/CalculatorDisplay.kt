package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorUiState
import com.example.calculator.math.AngleMode
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.LightGrey
import com.example.ui.theme.PrimaryYellow
import com.example.ui.theme.SnakeRed

@Composable
fun CalculatorDisplay(
    state: CalculatorUiState,
    onToggleAngleMode: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(state.expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGrey)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Row (DEG/RAD toggle, Memory badge, History button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Angle Mode Pill
                Surface(
                    onClick = onToggleAngleMode,
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryYellow.copy(alpha = 0.18f),
                    modifier = Modifier.testTag("angle_mode_toggle")
                ) {
                    Text(
                        text = if (state.angleMode == AngleMode.DEG) "DEG" else "RAD",
                        color = PrimaryYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Memory Active Badge
                if (state.memoryValue != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A84FF).copy(alpha = 0.2f),
                        modifier = Modifier.testTag("memory_badge")
                    ) {
                        Text(
                            text = "M",
                            color = Color(0xFF0A84FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // History Button
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("btn_history")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Calculation History",
                    tint = PrimaryYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Expression Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (state.expression.isEmpty()) "0" else state.expression,
                color = if (state.expression.isEmpty()) LightGrey else Color.White,
                fontSize = if (state.expression.length > 18) 20.sp else 26.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.testTag("calculator_expression")
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Result or Error Display
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            when {
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = SnakeRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("calculator_error")
                    )
                }
                state.previewResult.isNotEmpty() -> {
                    Text(
                        text = if (state.isEvaluated) "= ${state.previewResult}" else state.previewResult,
                        color = if (state.isEvaluated) PrimaryYellow else LightGrey,
                        fontSize = if (state.isEvaluated) 32.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.testTag("calculator_result")
                    )
                }
                else -> {
                    Text(
                        text = " ",
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}
