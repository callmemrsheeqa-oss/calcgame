package com.example.snake.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.settings.TiltSensitivity
import com.example.snake.model.SnakeDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

data class MotionTelemetry(
    val rollDeg: Float = 0f,
    val pitchDeg: Float = 0f,
    val deltaRoll: Float = 0f,
    val deltaPitch: Float = 0f,
    val activeDirection: SnakeDirection? = null,
    val isSensorAvailable: Boolean = true,
    val isCalibrated: Boolean = false
)

class MotionSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isSensorAvailable: Boolean = sensor != null

    private val _telemetry = MutableStateFlow(
        MotionTelemetry(isSensorAvailable = isSensorAvailable)
    )
    val telemetry: StateFlow<MotionTelemetry> = _telemetry.asStateFlow()

    private var neutralRoll: Float = 0f
    private var neutralPitch: Float = 0f
    private var isCalibrated: Boolean = false

    var sensitivity: TiltSensitivity = TiltSensitivity.MEDIUM
    var isEnabled: Boolean = true
    var invertDirection: Boolean = false

    // Low-pass filter weights
    private var filteredX = 0f
    private var filteredY = 0f
    private var filteredZ = 9.8f
    private val alpha = 0.25f

    fun startListening() {
        if (!isEnabled || sensor == null || sensorManager == null) return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun calibrateNeutral() {
        val current = _telemetry.value
        neutralRoll = current.rollDeg
        neutralPitch = current.pitchDeg
        isCalibrated = true
        _telemetry.value = current.copy(
            deltaRoll = 0f,
            deltaPitch = 0f,
            activeDirection = null,
            isCalibrated = true
        )
    }

    fun resetCalibration() {
        neutralRoll = 0f
        neutralPitch = 0f
        isCalibrated = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isEnabled) return

        // Low-pass filter on accelerometer axes
        filteredX = alpha * event.values[0] + (1 - alpha) * filteredX
        filteredY = alpha * event.values[1] + (1 - alpha) * filteredY
        filteredZ = alpha * event.values[2] + (1 - alpha) * filteredZ

        // Calculate pitch and roll angles in degrees
        // Roll: rotation around Y-axis (left/right tilt)
        // Pitch: rotation around X-axis (forward/backward tilt)
        val rollRad = atan2(-filteredX.toDouble(), sqrt((filteredY * filteredY + filteredZ * filteredZ).toDouble()))
        val pitchRad = atan2(filteredY.toDouble(), sqrt((filteredX * filteredX + filteredZ * filteredZ).toDouble()))

        val rollDeg = Math.toDegrees(rollRad).toFloat()
        val pitchDeg = Math.toDegrees(pitchRad).toFloat()

        val deltaRoll = rollDeg - neutralRoll
        val deltaPitch = pitchDeg - neutralPitch

        val threshold = sensitivity.thresholdDeg

        var dir: SnakeDirection? = when {
            // Dominant tilt on X vs Y
            kotlin.math.abs(deltaRoll) > kotlin.math.abs(deltaPitch) && kotlin.math.abs(deltaRoll) >= threshold -> {
                // Tilt right -> Move RIGHT; Tilt left -> Move LEFT
                if (deltaRoll > 0) SnakeDirection.RIGHT else SnakeDirection.LEFT
            }
            kotlin.math.abs(deltaPitch) >= threshold -> {
                // Tilt forward / away / top-down -> Move UP; Tilt backward / toward user -> Move DOWN
                if (deltaPitch > 0) SnakeDirection.UP else SnakeDirection.DOWN
            }
            else -> null
        }

        if (invertDirection && dir != null) {
            dir = when (dir) {
                SnakeDirection.UP -> SnakeDirection.DOWN
                SnakeDirection.DOWN -> SnakeDirection.UP
                SnakeDirection.LEFT -> SnakeDirection.RIGHT
                SnakeDirection.RIGHT -> SnakeDirection.LEFT
            }
        }

        _telemetry.value = MotionTelemetry(
            rollDeg = rollDeg,
            pitchDeg = pitchDeg,
            deltaRoll = deltaRoll,
            deltaPitch = deltaPitch,
            activeDirection = dir,
            isSensorAvailable = isSensorAvailable,
            isCalibrated = isCalibrated
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
