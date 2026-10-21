package com.example.calculator.math

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

enum class AngleMode {
    DEG,
    RAD
}

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}

class ExpressionEvaluator {

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: Char, val precedence: Int, val isRightAssociative: Boolean) : Token()
        data class Function(val name: String) : Token()
        data object OpenParen : Token()
        data object CloseParen : Token()
        data object Factorial : Token()
        data object Comma : Token()
    }

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): EvaluationResult {
        if (expression.isBlank()) {
            return EvaluationResult.Error("Empty expression")
        }

        return try {
            val sanitized = sanitize(expression)
            val tokens = tokenize(sanitized)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)
            if (result.isNaN()) {
                EvaluationResult.Error("Undefined")
            } else if (result.isInfinite()) {
                EvaluationResult.Error(if (result > 0) "Overflow" else "Underflow")
            } else {
                EvaluationResult.Success(result, formatResult(result))
            }
        } catch (e: ArithmeticException) {
            EvaluationResult.Error(e.message ?: "Math Error")
        } catch (e: IllegalArgumentException) {
            EvaluationResult.Error(e.message ?: "Invalid Expression")
        } catch (e: Exception) {
            EvaluationResult.Error("Invalid Expression")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace(" ", "")
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < input.length) {
            val c = input[i]

            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        sb.append(input[i])
                        i++
                    }
                    // Handle scientific notation e.g. 1e5 or 2.5E-3
                    if (i < input.length && (input[i] == 'e' || input[i] == 'E')) {
                        if (i + 1 < input.length && (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-')) {
                            sb.append(input[i])
                            i++
                            if (i < input.length && (input[i] == '+' || input[i] == '-')) {
                                sb.append(input[i])
                                i++
                            }
                            while (i < input.length && input[i].isDigit()) {
                                sb.append(input[i])
                                i++
                            }
                        }
                    }
                    val num = sb.toString().toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $sb")
                    tokens.add(Token.Number(num))
                    expectUnary = false
                    continue
                }

                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < input.length && (input[i].isLetter() || input[i].isDigit())) {
                        sb.append(input[i])
                        i++
                    }
                    val word = sb.toString()
                    when (word) {
                        "PI" -> {
                            if (!expectUnary) {
                                tokens.add(Token.Operator('*', 3, false))
                            }
                            tokens.add(Token.Number(Math.PI))
                            expectUnary = false
                        }
                        "e" -> {
                            if (!expectUnary) {
                                tokens.add(Token.Operator('*', 3, false))
                            }
                            tokens.add(Token.Number(Math.E))
                            expectUnary = false
                        }
                        else -> {
                            val lower = word.lowercase(Locale.ROOT)
                            if (!expectUnary) {
                                tokens.add(Token.Operator('*', 3, false))
                            }
                            tokens.add(Token.Function(lower))
                            expectUnary = true
                        }
                    }
                    continue
                }

                c == '(' -> {
                    if (!expectUnary) {
                        tokens.add(Token.Operator('*', 3, false))
                    }
                    tokens.add(Token.OpenParen)
                    expectUnary = true
                }

                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    expectUnary = false
                }

                c == '!' -> {
                    tokens.add(Token.Factorial)
                    expectUnary = false
                }

                c == ',' -> {
                    tokens.add(Token.Comma)
                    expectUnary = true
                }

                c == '+' -> {
                    if (!expectUnary) {
                        tokens.add(Token.Operator('+', 2, false))
                    }
                    expectUnary = true
                }

                c == '-' -> {
                    if (expectUnary) {
                        // Unary minus represented as ~
                        tokens.add(Token.Operator('~', 4, true))
                    } else {
                        tokens.add(Token.Operator('-', 2, false))
                        expectUnary = true
                    }
                }

                c == '*' -> {
                    tokens.add(Token.Operator('*', 3, false))
                    expectUnary = true
                }

                c == '/' -> {
                    tokens.add(Token.Operator('/', 3, false))
                    expectUnary = true
                }

                c == '%' -> {
                    tokens.add(Token.Operator('%', 3, false))
                    expectUnary = true
                }

                c == '^' -> {
                    tokens.add(Token.Operator('^', 5, true))
                    expectUnary = true
                }

                else -> {
                    throw IllegalArgumentException("Unknown character: $c")
                }
            }
            i++
        }

        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Function -> stack.addFirst(token)
                is Token.Factorial -> output.add(token)
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.first()
                        if (top is Token.Operator) {
                            val condition = if (token.isRightAssociative) {
                                token.precedence < top.precedence
                            } else {
                                token.precedence <= top.precedence
                            }
                            if (condition) {
                                output.add(stack.removeFirst())
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    stack.addFirst(token)
                }
                is Token.OpenParen -> stack.addFirst(token)
                is Token.CloseParen -> {
                    var foundOpen = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeFirst()
                        if (top is Token.OpenParen) {
                            foundOpen = true
                            break
                        }
                        output.add(top)
                    }
                    if (!foundOpen) throw IllegalArgumentException("Mismatched parentheses")
                    if (stack.isNotEmpty() && stack.first() is Token.Function) {
                        output.add(stack.removeFirst())
                    }
                }
                is Token.Comma -> {
                    while (stack.isNotEmpty() && stack.first() !is Token.OpenParen) {
                        output.add(stack.removeFirst())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeFirst()
            if (top is Token.OpenParen || top is Token.CloseParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRpn(rpn: List<Token>, angleMode: AngleMode): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.addFirst(token.value)
                is Token.Operator -> {
                    if (token.symbol == '~') {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                        val a = stack.removeFirst()
                        stack.addFirst(-a)
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid syntax")
                        val b = stack.removeFirst()
                        val a = stack.removeFirst()
                        val res = when (token.symbol) {
                            '+' -> a + b
                            '-' -> a - b
                            '*' -> a * b
                            '/' -> {
                                if (abs(b) < 1e-15) throw ArithmeticException("Division by zero")
                                a / b
                            }
                            '%' -> {
                                if (abs(b) < 1e-15) throw ArithmeticException("Modulo by zero")
                                a % b
                            }
                            '^' -> a.pow(b)
                            else -> throw IllegalArgumentException("Unknown operator: ${token.symbol}")
                        }
                        stack.addFirst(res)
                    }
                }
                is Token.Factorial -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val a = stack.removeFirst()
                    stack.addFirst(calculateFactorial(a))
                }
                is Token.Function -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing function argument")
                    val a = stack.removeFirst()
                    val res = evaluateFunction(token.name, a, angleMode)
                    stack.addFirst(res)
                }
                else -> throw IllegalArgumentException("Unexpected token in evaluation")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid expression")
        }
        return stack.removeFirst()
    }

    private fun evaluateFunction(name: String, arg: Double, angleMode: AngleMode): Double {
        val radArg = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg

        return when (name) {
            "sin" -> {
                if (angleMode == AngleMode.DEG) {
                    val modDeg = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        abs(modDeg - 0.0) < 1e-9 || abs(modDeg - 180.0) < 1e-9 || abs(modDeg - 360.0) < 1e-9 -> 0.0
                        abs(modDeg - 30.0) < 1e-9 || abs(modDeg - 150.0) < 1e-9 -> 0.5
                        abs(modDeg - 90.0) < 1e-9 -> 1.0
                        abs(modDeg - 210.0) < 1e-9 || abs(modDeg - 330.0) < 1e-9 -> -0.5
                        abs(modDeg - 270.0) < 1e-9 -> -1.0
                        else -> sin(radArg)
                    }
                } else {
                    sin(radArg)
                }
            }
            "cos" -> {
                if (angleMode == AngleMode.DEG) {
                    val modDeg = ((arg % 360.0) + 360.0) % 360.0
                    when {
                        abs(modDeg - 90.0) < 1e-9 || abs(modDeg - 270.0) < 1e-9 -> 0.0
                        abs(modDeg - 60.0) < 1e-9 || abs(modDeg - 300.0) < 1e-9 -> 0.5
                        abs(modDeg - 0.0) < 1e-9 || abs(modDeg - 360.0) < 1e-9 -> 1.0
                        abs(modDeg - 120.0) < 1e-9 || abs(modDeg - 240.0) < 1e-9 -> -0.5
                        abs(modDeg - 180.0) < 1e-9 -> -1.0
                        else -> cos(radArg)
                    }
                } else {
                    cos(radArg)
                }
            }
            "tan" -> {
                if (angleMode == AngleMode.DEG) {
                    val modDeg = ((arg % 180.0) + 180.0) % 180.0
                    if (abs(modDeg - 90.0) < 1e-9) throw ArithmeticException("Undefined (tan 90°)")
                    if (abs(modDeg - 45.0) < 1e-9) return 1.0
                    if (abs(modDeg - 135.0) < 1e-9) return -1.0
                    if (abs(modDeg - 0.0) < 1e-9 || abs(modDeg - 180.0) < 1e-9) return 0.0
                }
                val result = tan(radArg)
                if (abs(result) > 1e15) throw ArithmeticException("Undefined")
                result
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: asin([-1, 1])")
                val rad = asin(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error: acos([-1, 1])")
                val rad = acos(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "atan" -> {
                val rad = atan(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(rad) else rad
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "log" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error: log(x ≤ 0)")
                log10(arg)
            }
            "ln" -> {
                if (arg <= 0.0) throw ArithmeticException("Domain error: ln(x ≤ 0)")
                ln(arg)
            }
            "exp" -> exp(arg)
            "sqrt" -> {
                if (arg < 0.0) throw ArithmeticException("Domain error: √ of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            "floor" -> floor(arg)
            "ceil" -> ceil(arg)
            "round" -> round(arg)
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
    }

    private fun calculateFactorial(n: Double): Double {
        if (n < 0 || abs(n - n.roundToLong()) > 1e-9) {
            throw ArithmeticException("Factorial requires non-negative integer")
        }
        val intVal = n.roundToLong()
        if (intVal > 170) {
            throw ArithmeticException("Factorial overflow (> 170!)")
        }
        var result = 1.0
        for (i in 2..intVal) {
            result *= i
        }
        return result
    }

    private fun formatResult(value: Double): String {
        if (abs(value) < 1e-15) return "0"

        // Format to eliminate precision glitches like 0.30000000000000004
        val rounded = roundToSignificant(value, 12)
        if (rounded == rounded.toLong().toDouble() && abs(rounded) < 1e14) {
            return rounded.toLong().toString()
        }

        if (abs(rounded) >= 1e12 || (abs(rounded) < 1e-6 && abs(rounded) > 0)) {
            val formatSymbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("0.######E0", formatSymbols)
            return df.format(rounded).lowercase(Locale.US)
        }

        val formatSymbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##########", formatSymbols)
        df.isGroupingUsed = false
        return df.format(rounded)
    }

    private fun roundToSignificant(num: Double, digits: Int): Double {
        if (num == 0.0) return 0.0
        val magnitude = 10.0.pow(digits - 1 - floor(log10(abs(num))))
        return round(num * magnitude) / magnitude
    }
}
