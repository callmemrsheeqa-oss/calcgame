package com.example.snake.model

enum class SnakeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    fun isOpposite(other: SnakeDirection): Boolean {
        return (this == UP && other == DOWN) ||
                (this == DOWN && other == UP) ||
                (this == LEFT && other == RIGHT) ||
                (this == RIGHT && other == LEFT)
    }
}

data class GridPoint(
    val x: Int,
    val y: Int
)

enum class GameStatus {
    IDLE,
    RUNNING,
    PAUSED,
    GAME_OVER
}

data class FoodItem(
    val point: GridPoint,
    val isBonus: Boolean = false,
    val points: Int = if (isBonus) 25 else 10
)

data class SnakeGameState(
    val snake: List<GridPoint> = listOf(
        GridPoint(10, 12),
        GridPoint(10, 13),
        GridPoint(10, 14)
    ),
    val direction: SnakeDirection = SnakeDirection.UP,
    val food: FoodItem = FoodItem(GridPoint(10, 8)),
    val score: Int = 0,
    val highScore: Int = 0,
    val status: GameStatus = GameStatus.IDLE,
    val gridWidth: Int = 20,
    val gridHeight: Int = 24,
    val foodsEaten: Int = 0
)
