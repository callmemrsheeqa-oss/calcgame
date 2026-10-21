package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.*

enum class KeypadMode {
    BASIC,
    SCIENTIFIC
}

@Composable
fun CalculatorKeypad(
    onAppend: (String) -> Unit,
    onAppendFunction: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(KeypadMode.BASIC) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Selector Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkGrey)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeTabButton(
                title = "Standard",
                isSelected = mode == KeypadMode.BASIC,
                onClick = { mode = KeypadMode.BASIC },
                modifier = Modifier.weight(1f).testTag("tab_standard")
            )
            ModeTabButton(
                title = "Scientific (fx)",
                isSelected = mode == KeypadMode.SCIENTIFIC,
                onClick = { mode = KeypadMode.SCIENTIFIC },
                modifier = Modifier.weight(1f).testTag("tab_scientific")
            )
        }

        if (mode == KeypadMode.BASIC) {
            BasicKeypadGrid(
                onAppend = onAppend,
                onDelete = onDelete,
                onClear = onClear,
                onToggleSign = onToggleSign,
                onEquals = onEquals
            )
        } else {
            ScientificKeypadGrid(
                onAppend = onAppend,
                onAppendFunction = onAppendFunction,
                onDelete = onDelete,
                onClear = onClear,
                onEquals = onEquals,
                onMemoryClear = onMemoryClear,
                onMemoryRecall = onMemoryRecall,
                onMemoryAdd = onMemoryAdd,
                onMemorySubtract = onMemorySubtract
            )
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryYellow else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) DeepCharcoal else LightGrey,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun BasicKeypadGrid(
    onAppend: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit
) {
    val bHeight = 44.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Quick functions bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "(", onClick = { onAppend("(") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = ")", onClick = { onAppend(")") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "%", onClick = { onAppend("%") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "^", onClick = { onAppend("^") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = bHeight)
        }

        // Row 1: AC, DEL, +/-, ÷
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "AC", onClick = onClear, type = ButtonType.ACTION_CLEAR, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "DEL", onClick = onDelete, type = ButtonType.ACTION, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "+/−", onClick = onToggleSign, type = ButtonType.ACTION, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "÷", onClick = { onAppend("÷") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = bHeight)
        }

        // Row 2: 7, 8, 9, ×
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "7", onClick = { onAppend("7") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "8", onClick = { onAppend("8") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "9", onClick = { onAppend("9") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "×", onClick = { onAppend("×") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = bHeight)
        }

        // Row 3: 4, 5, 6, −
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "4", onClick = { onAppend("4") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "5", onClick = { onAppend("5") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "6", onClick = { onAppend("6") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "−", onClick = { onAppend("−") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = bHeight)
        }

        // Row 4: 1, 2, 3, +
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "1", onClick = { onAppend("1") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "2", onClick = { onAppend("2") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "3", onClick = { onAppend("3") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "+", onClick = { onAppend("+") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = bHeight)
        }

        // Row 5: 0, ., =
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "0", onClick = { onAppend("0") }, type = ButtonType.NUMBER, modifier = Modifier.weight(2f), height = bHeight)
            CalcButton(text = ".", onClick = { onAppend(".") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = bHeight)
            CalcButton(text = "=", onClick = onEquals, type = ButtonType.EQUALS, modifier = Modifier.weight(1f), height = bHeight)
        }
    }
}

@Composable
private fun ScientificKeypadGrid(
    onAppend: (String) -> Unit,
    onAppendFunction: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit
) {
    val sHeight = 35.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Memory Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "MC", onClick = onMemoryClear, type = ButtonType.MEMORY, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "MR", onClick = onMemoryRecall, type = ButtonType.MEMORY, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "M+", onClick = onMemoryAdd, type = ButtonType.MEMORY, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "M−", onClick = onMemorySubtract, type = ButtonType.MEMORY, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "AC", onClick = onClear, type = ButtonType.ACTION_CLEAR, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Trig Row 1
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "sin", onClick = { onAppendFunction("sin") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "cos", onClick = { onAppendFunction("cos") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "tan", onClick = { onAppendFunction("tan") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "π", onClick = { onAppend("π") }, type = ButtonType.CONSTANT, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "DEL", onClick = onDelete, type = ButtonType.ACTION, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Trig Row 2 (Inverse)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "asin", onClick = { onAppendFunction("asin") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "acos", onClick = { onAppendFunction("acos") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "atan", onClick = { onAppendFunction("atan") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "e", onClick = { onAppend("e") }, type = ButtonType.CONSTANT, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "÷", onClick = { onAppend("÷") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Hyperbolic & Logs
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "sinh", onClick = { onAppendFunction("sinh") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "cosh", onClick = { onAppendFunction("cosh") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "tanh", onClick = { onAppendFunction("tanh") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "log", onClick = { onAppendFunction("log") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "×", onClick = { onAppend("×") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Exponentials, Powers & Roots
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "ln", onClick = { onAppendFunction("ln") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "√x", onClick = { onAppendFunction("sqrt") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "x²", onClick = { onAppend("^2") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "xʸ", onClick = { onAppend("^") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "−", onClick = { onAppend("−") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Additional Math: Factorial, Abs, Reciprocal, Round
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "n!", onClick = { onAppend("!") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "|x|", onClick = { onAppendFunction("abs") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "1/x", onClick = { onAppend("1÷(") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "round", onClick = { onAppendFunction("round") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "+", onClick = { onAppend("+") }, type = ButtonType.OPERATOR, modifier = Modifier.weight(1f), height = sHeight)
        }

        // Quick number & equals row
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CalcButton(text = "(", onClick = { onAppend("(") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = ")", onClick = { onAppend(")") }, type = ButtonType.FUNCTION, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "0", onClick = { onAppend("0") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = ".", onClick = { onAppend(".") }, type = ButtonType.NUMBER, modifier = Modifier.weight(1f), height = sHeight)
            CalcButton(text = "=", onClick = onEquals, type = ButtonType.EQUALS, modifier = Modifier.weight(1f), height = sHeight)
        }
    }
}

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    CONSTANT,
    MEMORY,
    ACTION,
    ACTION_CLEAR,
    EQUALS
}

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    type: ButtonType,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 46.dp
) {
    val bgColor = when (type) {
        ButtonType.NUMBER -> DarkGrey
        ButtonType.OPERATOR -> MidGrey
        ButtonType.FUNCTION -> Color(0xFF232323)
        ButtonType.CONSTANT -> Color(0xFF2A2A2A)
        ButtonType.MEMORY -> Color(0xFF1E2838)
        ButtonType.ACTION -> MidGrey
        ButtonType.ACTION_CLEAR -> Color(0xFF3B1E1E)
        ButtonType.EQUALS -> PrimaryYellow
    }

    val contentColor = when (type) {
        ButtonType.NUMBER -> Color.White
        ButtonType.OPERATOR -> SecondaryYellow
        ButtonType.FUNCTION -> Color(0xFFFFE066)
        ButtonType.CONSTANT -> PrimaryYellow
        ButtonType.MEMORY -> Color(0xFF5AC8FA)
        ButtonType.ACTION -> Color.White
        ButtonType.ACTION_CLEAR -> SnakeRed
        ButtonType.EQUALS -> DeepCharcoal
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .testTag("btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = if (text.length > 3) 12.sp else if (text.length > 1) 14.sp else 18.sp,
            fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.NUMBER) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
