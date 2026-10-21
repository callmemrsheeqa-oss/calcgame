package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.calculator.math.AngleMode
import com.example.calculator.model.CalculationHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class TiltSensitivity(val displayName: String, val thresholdDeg: Float) {
    LOW("Low", 18f),
    MEDIUM("Medium", 11f),
    HIGH("High", 6f)
}

enum class GameSpeed(val displayName: String, val tickIntervalMs: Long) {
    SLOW("Slow", 210L),
    NORMAL("Normal", 145L),
    FAST("Fast", 90L)
}

data class AppSettings(
    val motionControlsEnabled: Boolean = false,
    val tiltSensitivity: TiltSensitivity = TiltSensitivity.MEDIUM,
    val gameSpeed: GameSpeed = GameSpeed.NORMAL,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val highScore: Int = 0,
    val angleMode: AngleMode = AngleMode.DEG,
    val invertTiltDirection: Boolean = false
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calcsnake_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<CalculationHistoryItem>> = _history.asStateFlow()

    private fun loadSettings(): AppSettings {
        val motion = prefs.getBoolean("motion_controls", false)
        val sensitivityStr = prefs.getString("tilt_sensitivity", TiltSensitivity.MEDIUM.name) ?: TiltSensitivity.MEDIUM.name
        val sensitivity = try { TiltSensitivity.valueOf(sensitivityStr) } catch (_: Exception) { TiltSensitivity.MEDIUM }
        val speedStr = prefs.getString("game_speed", GameSpeed.NORMAL.name) ?: GameSpeed.NORMAL.name
        val speed = try { GameSpeed.valueOf(speedStr) } catch (_: Exception) { GameSpeed.NORMAL }
        val sound = prefs.getBoolean("sound_enabled", true)
        val vibration = prefs.getBoolean("vibration_enabled", true)
        val highScore = prefs.getInt("high_score", 0)
        val angleStr = prefs.getString("angle_mode", AngleMode.DEG.name) ?: AngleMode.DEG.name
        val angleMode = try { AngleMode.valueOf(angleStr) } catch (_: Exception) { AngleMode.DEG }
        val invertTilt = prefs.getBoolean("invert_tilt_direction", false)

        return AppSettings(
            motionControlsEnabled = motion,
            tiltSensitivity = sensitivity,
            gameSpeed = speed,
            soundEnabled = sound,
            vibrationEnabled = vibration,
            highScore = highScore,
            angleMode = angleMode,
            invertTiltDirection = invertTilt
        )
    }

    fun setInvertTiltDirection(invert: Boolean) {
        prefs.edit().putBoolean("invert_tilt_direction", invert).apply()
        _settings.value = _settings.value.copy(invertTiltDirection = invert)
    }

    fun setMotionControlsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("motion_controls", enabled).apply()
        _settings.value = _settings.value.copy(motionControlsEnabled = enabled)
    }

    fun setTiltSensitivity(sensitivity: TiltSensitivity) {
        prefs.edit().putString("tilt_sensitivity", sensitivity.name).apply()
        _settings.value = _settings.value.copy(tiltSensitivity = sensitivity)
    }

    fun setGameSpeed(speed: GameSpeed) {
        prefs.edit().putString("game_speed", speed.name).apply()
        _settings.value = _settings.value.copy(gameSpeed = speed)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _settings.value = _settings.value.copy(vibrationEnabled = enabled)
    }

    fun setAngleMode(angleMode: AngleMode) {
        prefs.edit().putString("angle_mode", angleMode.name).apply()
        _settings.value = _settings.value.copy(angleMode = angleMode)
    }

    fun updateHighScore(score: Int) {
        if (score > _settings.value.highScore) {
            prefs.edit().putInt("high_score", score).apply()
            _settings.value = _settings.value.copy(highScore = score)
        }
    }

    fun resetHighScore() {
        prefs.edit().putInt("high_score", 0).apply()
        _settings.value = _settings.value.copy(highScore = 0)
    }

    private fun loadHistory(): List<CalculationHistoryItem> {
        val json = prefs.getString("calc_history", "[]") ?: "[]"
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<CalculationHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalculationHistoryItem(
                        id = obj.getString("id"),
                        expression = obj.getString("expression"),
                        result = obj.getString("result"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addHistoryItem(item: CalculationHistoryItem) {
        val current = _history.value.toMutableList()
        current.add(0, item)
        val trimmed = if (current.size > 50) current.subList(0, 50) else current
        _history.value = trimmed
        saveHistory(trimmed)
    }

    fun clearHistory() {
        _history.value = emptyList()
        saveHistory(emptyList())
    }

    private fun saveHistory(list: List<CalculationHistoryItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("calc_history", array.toString()).apply()
    }
}
