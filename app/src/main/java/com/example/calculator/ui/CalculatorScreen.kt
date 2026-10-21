package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display area
        CalculatorDisplay(
            state = state,
            onToggleAngleMode = { viewModel.toggleAngleMode() },
            onOpenHistory = { showHistory = true },
            modifier = Modifier.weight(0.35f, fill = false)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Keypad area
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
            modifier = Modifier.weight(0.65f, fill = false)
        )
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
