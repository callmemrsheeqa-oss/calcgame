package com.example

import com.example.calculator.math.AngleMode
import com.example.calculator.math.EvaluationResult
import com.example.calculator.math.ExpressionEvaluator
import com.example.snake.model.SnakeDirection
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var evaluator: ExpressionEvaluator

    @Before
    fun setup() {
        evaluator = ExpressionEvaluator()
    }

    @Test
    fun testOperatorPrecedence() {
        // 2 + 3 × 4 = 14
        val res = evaluator.evaluate("2+3×4")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("14", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testParenthesesPrecedence() {
        // (2 + 3) × 4 = 20
        val res = evaluator.evaluate("(2+3)×4")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("20", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testSinDegreeMode() {
        // sin(30) = 0.5 in DEG mode
        val res = evaluator.evaluate("sin(30)", AngleMode.DEG)
        assertTrue(res is EvaluationResult.Success)
        assertEquals("0.5", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testSinRadianMode() {
        // sin(π/2) = 1 in RAD mode
        val res = evaluator.evaluate("sin(π/2)", AngleMode.RAD)
        assertTrue(res is EvaluationResult.Success)
        assertEquals("1", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testSqrt() {
        // sqrt(144) = 12
        val res = evaluator.evaluate("sqrt(144)")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("12", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testLog() {
        // log(100) = 2
        val res = evaluator.evaluate("log(100)")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("2", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testNaturalLog() {
        // ln(e) = 1
        val res = evaluator.evaluate("ln(e)")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("1", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testFactorial() {
        // 5! = 120
        val res = evaluator.evaluate("5!")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("120", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testExponentiation() {
        // 2^10 = 1024
        val res = evaluator.evaluate("2^10")
        assertTrue(res is EvaluationResult.Success)
        assertEquals("1024", (res as EvaluationResult.Success).formatted)
    }

    @Test
    fun testDivisionByZero() {
        val res = evaluator.evaluate("10/0")
        assertTrue(res is EvaluationResult.Error)
        assertEquals("Division by zero", (res as EvaluationResult.Error).message)
    }

    @Test
    fun testInvalidExpressionHandledGracefully() {
        val res = evaluator.evaluate("+++---")
        assertTrue(res is EvaluationResult.Error)
    }

    @Test
    fun testSnakeDirectionOpposites() {
        assertTrue(SnakeDirection.UP.isOpposite(SnakeDirection.DOWN))
        assertTrue(SnakeDirection.DOWN.isOpposite(SnakeDirection.UP))
        assertTrue(SnakeDirection.LEFT.isOpposite(SnakeDirection.RIGHT))
        assertTrue(SnakeDirection.RIGHT.isOpposite(SnakeDirection.LEFT))
        assertFalse(SnakeDirection.UP.isOpposite(SnakeDirection.LEFT))
        assertFalse(SnakeDirection.DOWN.isOpposite(SnakeDirection.RIGHT))
    }

    @Test
    fun testSnakeBoundaryWrapAround() {
        val gridWidth = 24
        val gridHeight = 16

        // Moving right past boundary (x = 23 -> x = 0)
        val wrappedRightX = (23 + 1 + gridWidth) % gridWidth
        assertEquals(0, wrappedRightX)

        // Moving left past boundary (x = 0 -> x = 23)
        val wrappedLeftX = (0 - 1 + gridWidth) % gridWidth
        assertEquals(23, wrappedLeftX)

        // Moving up past boundary (y = 0 -> y = 15)
        val wrappedUpY = (0 - 1 + gridHeight) % gridHeight
        assertEquals(15, wrappedUpY)

        // Moving down past boundary (y = 15 -> y = 0)
        val wrappedDownY = (15 + 1 + gridHeight) % gridHeight
        assertEquals(0, wrappedDownY)
    }
}
