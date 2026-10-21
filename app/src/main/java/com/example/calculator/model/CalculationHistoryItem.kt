package com.example.calculator.model

data class CalculationHistoryItem(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
