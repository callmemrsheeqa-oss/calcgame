package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calculator.CalculatorViewModel
import com.example.settings.SettingsManager
import com.example.snake.SnakeViewModel
import com.example.snake.sensor.MotionSensorManager
import com.example.ui.CalcSnakeApp
import com.example.ui.theme.CalcSnakeTheme
import com.example.util.HapticManager
import com.example.util.SoundManager

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var motionSensorManager: MotionSensorManager
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager

    private lateinit var calculatorViewModel: CalculatorViewModel
    private lateinit var snakeViewModel: SnakeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsManager = SettingsManager(applicationContext)
        motionSensorManager = MotionSensorManager(applicationContext)
        soundManager = SoundManager()
        hapticManager = HapticManager(applicationContext)

        calculatorViewModel = CalculatorViewModel(settingsManager, soundManager, hapticManager)
        snakeViewModel = SnakeViewModel(settingsManager, motionSensorManager, soundManager, hapticManager)

        setContent {
            CalcSnakeTheme(darkTheme = true) {
                CalcSnakeApp(
                    calculatorViewModel = calculatorViewModel,
                    snakeViewModel = snakeViewModel,
                    settingsManager = settingsManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (settingsManager.settings.value.motionControlsEnabled) {
            motionSensorManager.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        motionSensorManager.stopListening()
        snakeViewModel.pauseGame()
    }
}
