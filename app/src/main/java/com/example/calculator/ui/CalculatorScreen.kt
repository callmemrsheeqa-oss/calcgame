package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display area
            CalculatorDisplay(
                state = state,
                onToggleAngleMode = { viewModel.toggleAngleMode() },
                onOpenHistory = { showHistory = true }
            )

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
                onMemorySubtract = { viewModel.onMemorySubtract() }
            )
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
