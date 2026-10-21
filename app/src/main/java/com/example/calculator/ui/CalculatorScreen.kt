package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.CalculatorViewModel

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showHistory by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val isLandscape = screenWidth > screenHeight
        val isWide = screenWidth >= 600.dp || (isLandscape && screenWidth >= 520.dp)

        if (isWide) {
            // Tablet / Landscape: Side-by-Side Two Pane Responsive Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pane: Display & History
                Box(
                    modifier = Modifier
                        .weight(0.38f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CalculatorDisplay(
                        state = state,
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        onOpenHistory = { showHistory = true },
                        modifier = Modifier.fillMaxHeight()
                    )
                }

                // Right Pane: Full Comprehensive Keypad
                val wideButtonHeight = ((screenHeight - 48.dp) / 5f).coerceIn(34.dp, 58.dp)
                Box(
                    modifier = Modifier
                        .weight(0.62f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CalculatorKeypad(
                        onAppend = { viewModel.onAppend(it) },
                        onAppendFunction = { viewModel.onAppendFunction(it) },
                        onDelete = { viewModel.onDelete() },
                        onClear = { viewModel.onClear() },
                        onToggleSign = { viewModel.onToggleSign() },
                        onEquals = { viewModel.onEquals() },
                        onMemoryClear = { viewModel.onMemoryClear() },
                        onMemoryRecall = { viewModel.onMemoryRecall() },
                        onMemoryAdd = { viewModel.onMemoryAdd() },
                        onMemorySubtract = { viewModel.onMemorySubtract() },
                        isWide = true,
                        customButtonHeight = wideButtonHeight
                    )
                }
            }
        } else {
            // Portrait Mobile: Dynamically Sized Vertical Layout with Scroll Safeguard
            val availableKeypadSpace = (screenHeight - 160.dp).coerceAtLeast(220.dp)
            val dynamicButtonHeight = (availableKeypadSpace / 7.4f).coerceIn(32.dp, 52.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display Area
                CalculatorDisplay(
                    state = state,
                    onToggleAngleMode = { viewModel.toggleAngleMode() },
                    onOpenHistory = { showHistory = true },
                    modifier = Modifier.widthIn(max = 600.dp)
                )

                // Keypad Area
                CalculatorKeypad(
                    onAppend = { viewModel.onAppend(it) },
                    onAppendFunction = { viewModel.onAppendFunction(it) },
                    onDelete = { viewModel.onDelete() },
                    onClear = { viewModel.onClear() },
                    onToggleSign = { viewModel.onToggleSign() },
                    onEquals = { viewModel.onEquals() },
                    onMemoryClear = { viewModel.onMemoryClear() },
                    onMemoryRecall = { viewModel.onMemoryRecall() },
                    onMemoryAdd = { viewModel.onMemoryAdd() },
                    onMemorySubtract = { viewModel.onMemorySubtract() },
                    isWide = false,
                    customButtonHeight = dynamicButtonHeight,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            }
        }
    }

    if (showHistory) {
        CalculationHistorySheet(
            history = state.history,
            onSelectExpression = {
                viewModel.useHistoryItem(it)
                showHistory = false
            },
            onInsertResult = {
                viewModel.insertHistoryResult(it)
                showHistory = false
            },
            onClearHistory = {
                viewModel.clearHistory()
            },
            onDismiss = {
                showHistory = false
            }
        )
    }
}
