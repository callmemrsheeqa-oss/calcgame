package com.example.snake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.settings.GameSpeed
import com.example.settings.SettingsManager
import com.example.snake.model.*
import com.example.snake.sensor.MotionSensorManager
import com.example.snake.sensor.MotionTelemetry
import com.example.util.HapticManager
import com.example.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SnakeViewModel(
    private val settingsManager: SettingsManager,
    private val motionSensorManager: MotionSensorManager,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val gridWidth = 24
    private val gridHeight = 16

    private val _state = MutableStateFlow(
        SnakeGameState(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            highScore = settingsManager.settings.value.highScore,
            snake = initialSnake(),
            direction = SnakeDirection.RIGHT,
            food = generateFood(initialSnake())
        )
    )
    val state: StateFlow<SnakeGameState> = _state.asStateFlow()

    val telemetry: StateFlow<MotionTelemetry> = motionSensorManager.telemetry

    private var nextDirection: SnakeDirection = SnakeDirection.RIGHT
    private var gameLoopJob: Job? = null
    private var lastTouchInputTime: Long = 0L

    init {
        // Synchronize settings
        viewModelScope.launch {
            settingsManager.settings.collect { settings ->
                _state.value = _state.value.copy(highScore = settings.highScore)
                motionSensorManager.sensitivity = settings.tiltSensitivity
                motionSensorManager.isEnabled = settings.motionControlsEnabled
                motionSensorManager.invertDirection = settings.invertTiltDirection
                soundManager.isEnabled = settings.soundEnabled
                hapticManager.isEnabled = settings.vibrationEnabled
                if (!settings.motionControlsEnabled) {
                    motionSensorManager.stopListening()
                }
            }
        }

        // Listen to tilt telemetry for motion steering ONLY if motion is enabled and no recent touch input
        viewModelScope.launch {
            telemetry.collect { telem ->
                val now = System.currentTimeMillis()
                val isMotionActive = settingsManager.settings.value.motionControlsEnabled
                val touchCooldownPassed = (now - lastTouchInputTime) > 3000L
                if (_state.value.status == GameStatus.RUNNING && isMotionActive && touchCooldownPassed) {
                    val tiltDir = telem.activeDirection
                    if (tiltDir != null) {
                        queueDirection(tiltDir, fromTouch = false)
                    }
                }
            }
        }
    }

    private fun initialSnake(): List<GridPoint> {
        val startX = gridWidth / 3
        val startY = gridHeight / 2
        return listOf(
            GridPoint(startX, startY),
            GridPoint(startX - 1, startY),
            GridPoint(startX - 2, startY)
        )
    }

    fun startGame() {
        val currentSnake = initialSnake()
        nextDirection = SnakeDirection.RIGHT
        _state.value = SnakeGameState(
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            highScore = settingsManager.settings.value.highScore,
            snake = currentSnake,
            direction = SnakeDirection.RIGHT,
            food = generateFood(currentSnake),
            score = 0,
            status = GameStatus.RUNNING,
            foodsEaten = 0
        )
        if (settingsManager.settings.value.motionControlsEnabled) {
            motionSensorManager.startListening()
        }
        startGameLoop()
    }

    fun pauseGame() {
        if (_state.value.status == GameStatus.RUNNING) {
            _state.value = _state.value.copy(status = GameStatus.PAUSED)
            gameLoopJob?.cancel()
            motionSensorManager.stopListening()
        }
    }

    fun resumeGame() {
        if (_state.value.status == GameStatus.PAUSED) {
            _state.value = _state.value.copy(status = GameStatus.RUNNING)
            if (settingsManager.settings.value.motionControlsEnabled) {
                motionSensorManager.startListening()
            }
            startGameLoop()
        }
    }

    fun restartGame() {
        gameLoopJob?.cancel()
        startGame()
    }

    fun queueDirection(newDir: SnakeDirection, fromTouch: Boolean = true) {
        if (fromTouch) {
            lastTouchInputTime = System.currentTimeMillis()
        }
        if (_state.value.status == GameStatus.IDLE) {
            startGame()
        }
        if (_state.value.status != GameStatus.RUNNING) return

        val currentDir = _state.value.direction
        // Prevent reversing directly into own body
        if (!currentDir.isOpposite(newDir)) {
            nextDirection = newDir
        }
    }

    fun calibrateSensors() {
        motionSensorManager.calibrateNeutral()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (isActive && _state.value.status == GameStatus.RUNNING) {
                val interval = settingsManager.settings.value.gameSpeed.tickIntervalMs
                delay(interval)
                tick()
            }
        }
    }

    private fun tick() {
        val currentState = _state.value
        if (currentState.status != GameStatus.RUNNING) return

        val direction = nextDirection
        val head = currentState.snake.first()
        val rawX = when (direction) {
            SnakeDirection.LEFT -> head.x - 1
            SnakeDirection.RIGHT -> head.x + 1
            else -> head.x
        }
        val rawY = when (direction) {
            SnakeDirection.UP -> head.y - 1
            SnakeDirection.DOWN -> head.y + 1
            else -> head.y
        }

        // BORDERLESS WRAP-AROUND: Snake emerges from opposite side upon reaching boundary
        val wrappedX = (rawX + currentState.gridWidth) % currentState.gridWidth
        val wrappedY = (rawY + currentState.gridHeight) % currentState.gridHeight
        val newHead = GridPoint(wrappedX, wrappedY)

        // Check Self Collision only
        if (currentState.snake.dropLast(1).contains(newHead)) {
            handleGameOver()
            return
        }

        // Check Food Collision
        val ateFood = newHead == currentState.food.point
        val newSnake = mutableListOf<GridPoint>()
        newSnake.add(newHead)

        if (ateFood) {
            newSnake.addAll(currentState.snake)
            val addedScore = currentState.food.points
            val newScore = currentState.score + addedScore
            val eatenCount = currentState.foodsEaten + 1

            if (currentState.food.isBonus) {
                soundManager.playBonusEat()
            } else {
                soundManager.playEat()
            }
            hapticManager.vibrateEat()

            val newHighScore = maxOf(currentState.highScore, newScore)
            settingsManager.updateHighScore(newScore)

            val nextFood = generateFood(newSnake, eatenCount)

            _state.value = currentState.copy(
                snake = newSnake,
                direction = direction,
                food = nextFood,
                score = newScore,
                highScore = newHighScore,
                foodsEaten = eatenCount
            )
        } else {
            newSnake.addAll(currentState.snake.dropLast(1))
            _state.value = currentState.copy(
                snake = newSnake,
                direction = direction
            )
        }
    }

    private fun handleGameOver() {
        gameLoopJob?.cancel()
        soundManager.playGameOver()
        hapticManager.vibrateGameOver()
        motionSensorManager.stopListening()
        _state.value = _state.value.copy(status = GameStatus.GAME_OVER)
    }

    private fun generateFood(snake: List<GridPoint>, count: Int = 0): FoodItem {
        val emptyPoints = mutableListOf<GridPoint>()
        for (x in 0 until gridWidth) {
            for (y in 0 until gridHeight) {
                val p = GridPoint(x, y)
                if (!snake.contains(p)) {
                    emptyPoints.add(p)
                }
            }
        }
        val targetPoint = if (emptyPoints.isNotEmpty()) {
            emptyPoints[Random.nextInt(emptyPoints.size)]
        } else {
            GridPoint(0, 0)
        }
        // Spawn bonus golden food occasionally (every 5th food)
        val isBonus = count > 0 && count % 5 == 0
        return FoodItem(targetPoint, isBonus = isBonus)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        motionSensorManager.stopListening()
    }
}
