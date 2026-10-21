package com.example.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.math.AngleMode
import com.example.calculator.math.EvaluationResult
import com.example.calculator.math.ExpressionEvaluator
import com.example.calculator.model.CalculationHistoryItem
import com.example.settings.SettingsManager
import com.example.util.HapticManager
import com.example.util.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val error: String? = null,
    val angleMode: AngleMode = AngleMode.DEG,
    val memoryValue: Double? = null,
    val isEvaluated: Boolean = false,
    val history: List<CalculationHistoryItem> = emptyList()
)

class CalculatorViewModel(
    private val settingsManager: SettingsManager,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val evaluator = ExpressionEvaluator()

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            angleMode = settingsManager.settings.value.angleMode,
            history = settingsManager.history.value
        )
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(angleMode = settings.angleMode)
                soundManager.isEnabled = settings.soundEnabled
                hapticManager.isEnabled = settings.vibrationEnabled
            }
        }
        viewModelScope.launch {
            settingsManager.history.collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }
    }

    private fun triggerFeedback() {
        soundManager.playClick()
        hapticManager.vibrateClick()
    }

    fun onAppend(text: String) {
        triggerFeedback()
        val current = _uiState.value
        val newExpr = if (current.isEvaluated && !isOperator(text)) {
            // Started typing a new number after evaluation
            text
        } else if (current.isEvaluated && isOperator(text)) {
            // Chained operation from previous result
            val previousResult = current.previewResult.ifEmpty { "0" }
            previousResult + text
        } else {
            current.expression + text
        }

        updateExpression(newExpr)
    }

    private fun isOperator(text: String): Boolean {
        return text in listOf("+", "−", "×", "÷", "%", "^", "!")
    }

    fun onAppendFunction(funcName: String) {
        triggerFeedback()
        val current = _uiState.value
        val newExpr = if (current.isEvaluated) {
            "$funcName("
        } else {
            current.expression + "$funcName("
        }
        updateExpression(newExpr)
    }

    fun onDelete() {
        triggerFeedback()
        val current = _uiState.value
        if (current.expression.isEmpty()) return

        // Smart deletion of multi-character functions e.g. "sin(", "sqrt("
        var expr = current.expression
        val funcs = listOf("asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(", "sqrt(", "cbrt(", "floor(", "round(", "ceil(", "sin(", "cos(", "tan(", "log(", "ln(", "abs(")
        var removedFunc = false
        for (f in funcs) {
            if (expr.endsWith(f)) {
                expr = expr.dropLast(f.length)
                removedFunc = true
                break
            }
        }
        if (!removedFunc) {
            expr = expr.dropLast(1)
        }

        _uiState.value = current.copy(
            expression = expr,
            isEvaluated = false,
            error = null
        )
        updateLivePreview(expr, current.angleMode)
    }

    fun onClear() {
        triggerFeedback()
        _uiState.value = _uiState.value.copy(
            expression = "",
            previewResult = "",
            error = null,
            isEvaluated = false
        )
    }

    fun onToggleSign() {
        triggerFeedback()
        val current = _uiState.value
        var expr = current.expression
        if (expr.isEmpty()) {
            expr = "−"
        } else if (expr.endsWith("−")) {
            expr = expr.dropLast(1)
        } else if (expr.endsWith("+")) {
            expr = expr.dropLast(1) + "−"
        } else {
            expr += "×(-1)"
        }
        updateExpression(expr)
    }

    fun onEquals() {
        triggerFeedback()
        val current = _uiState.value
        if (current.expression.isBlank()) return

        when (val res = evaluator.evaluate(current.expression, current.angleMode)) {
            is EvaluationResult.Success -> {
                val item = CalculationHistoryItem(
                    expression = current.expression,
                    result = res.formatted
                )
                settingsManager.addHistoryItem(item)
                _uiState.value = current.copy(
                    previewResult = res.formatted,
                    error = null,
                    isEvaluated = true
                )
            }
            is EvaluationResult.Error -> {
                _uiState.value = current.copy(
                    error = res.message,
                    isEvaluated = true
                )
            }
        }
    }

    fun toggleAngleMode() {
        triggerFeedback()
        val newMode = if (_uiState.value.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        settingsManager.setAngleMode(newMode)
        _uiState.value = _uiState.value.copy(angleMode = newMode)
        updateLivePreview(_uiState.value.expression, newMode)
    }

    fun onMemoryClear() {
        triggerFeedback()
        _uiState.value = _uiState.value.copy(memoryValue = null)
    }

    fun onMemoryRecall() {
        triggerFeedback()
        val mem = _uiState.value.memoryValue ?: return
        val memStr = if (mem == mem.toLong().toDouble()) mem.toLong().toString() else mem.toString()
        onAppend(memStr)
    }

    fun onMemoryAdd() {
        triggerFeedback()
        val currentVal = getCurrentNumericValue() ?: return
        val existing = _uiState.value.memoryValue ?: 0.0
        _uiState.value = _uiState.value.copy(memoryValue = existing + currentVal)
    }

    fun onMemorySubtract() {
        triggerFeedback()
        val currentVal = getCurrentNumericValue() ?: return
        val existing = _uiState.value.memoryValue ?: 0.0
        _uiState.value = _uiState.value.copy(memoryValue = existing - currentVal)
    }

    private fun getCurrentNumericValue(): Double? {
        val current = _uiState.value
        if (current.previewResult.isNotEmpty() && current.error == null) {
            return current.previewResult.toDoubleOrNull()
        }
        if (current.expression.isNotEmpty()) {
            val eval = evaluator.evaluate(current.expression, current.angleMode)
            if (eval is EvaluationResult.Success) {
                return eval.value
            }
        }
        return null
    }

    fun useHistoryItem(item: CalculationHistoryItem) {
        triggerFeedback()
        _uiState.value = _uiState.value.copy(
            expression = item.expression,
            previewResult = item.result,
            isEvaluated = true,
            error = null
        )
    }

    fun insertHistoryResult(result: String) {
        triggerFeedback()
        onAppend(result)
    }

    fun clearHistory() {
        triggerFeedback()
        settingsManager.clearHistory()
    }

    private fun updateExpression(newExpr: String) {
        _uiState.value = _uiState.value.copy(
            expression = newExpr,
            isEvaluated = false,
            error = null
        )
        updateLivePreview(newExpr, _uiState.value.angleMode)
    }

    private fun updateLivePreview(expr: String, angleMode: AngleMode) {
        if (expr.isBlank()) {
            _uiState.value = _uiState.value.copy(previewResult = "", error = null)
            return
        }
        val result = evaluator.evaluate(expr, angleMode)
        if (result is EvaluationResult.Success) {
            _uiState.value = _uiState.value.copy(previewResult = result.formatted, error = null)
        }
    }
}
