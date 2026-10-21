# CalcSnake — Scientific Calculator & Motion-Controlled Snake Game

CalcSnake is a modern, high-performance Android application built with Kotlin and Jetpack Compose. It seamlessly fuses an advanced, precision scientific calculator with a classic, tilt-controlled Snake Game driven by device accelerometer and orientation sensors.

---

## 📸 Key Features

### 1. 🧮 Comprehensive Scientific Calculator
- **Full Standard Arithmetic:** Addition (`+`), subtraction (`−`), multiplication (`×`), division (`÷`), modulo (`%`), sign inversion (`+/−`), parentheses, clear all (`AC`), and delete (`DEL`).
- **Mathematical Precedence & Operator Hierarchy:** Full AST / Shunting-yard expression parsing ensuring proper precedence (e.g., `2 + 3 × 4 = 14`, `(2 + 3) × 4 = 20`).
- **Advanced Trigonometric & Hyperbolic Suite:**
  - Standard: `sin`, `cos`, `tan`
  - Inverse: `asin`, `acos`, `atan`
  - Hyperbolic: `sinh`, `cosh`, `tanh`
- **Exponentials & Logarithms:** `log` (base 10), `ln` (natural), `eˣ`, `xʸ`, `x²`, `x³`, `√x`, `∛x`.
- **Special Functions & Constants:**
  - Factorials (`n!`), absolute values (`|x|`), reciprocals (`1/x`), rounding (`round`, `floor`, `ceil`).
  - Constants: Pi (`π`) and Euler's number (`e`).
- **Angle Modes:** Clear toggle between **Degrees (DEG)** and **Radians (RAD)** with exact real-time angle adjustments (e.g., `sin(30) = 0.5` in DEG; `sin(π/2) = 1` in RAD).
- **Memory Operations:** `MC` (Clear), `MR` (Recall), `M+` (Add), and `M−` (Subtract) with an active on-screen indicator badge.
- **Calculation History:** Scrollable log of past calculations with instant tap-to-reuse expressions or insert results, with local persistence.
- **Graceful Error Handling:** Protective guards for division-by-zero, negative square roots, and domain overflow.

### 2. 🐍 Motion-Controlled Classic Snake Game
- **Vivid Aesthetic:** Distinct bright red snake (`#FF2D2D`) with expressive head direction styling against a deep charcoal board (`#171717`) framed by glowing golden yellow accents (`#FFD60A`).
- **Special Motion Tilt Controls:**
  - Real-time phone tilting using hardware accelerometer telemetry.
  - Tilt left/right to steer snake left/right.
  - Tilt forward (away) or backward (toward user) to steer snake up/down.
  - **Calibrate Neutral Orientation:** Instantly zero the resting angle while holding your phone in any natural posture.
  - Configurable sensitivity (**Low**, **Medium**, **High**) with low-pass filtering to eliminate hand jitter.
  - **180° Reversal Protection:** Prevents the snake from accidentally turning back into its own body.
- **Responsive D-Pad Fallback:** On-screen ergonomic D-pad and gesture controls ensuring full playability on emulators, tablets, or when motion sensors are disabled.
- **Engaging Gameplay:** Random food spawning (purple berries & bonus golden apples), score tracking, persistent high score records, synthesized retro audio beeps, and tactile haptic vibration.

### 3. ⚙️ Settings & Customization
- Toggle Motion Controls on/off.
- Configurable Tilt Sensitivity (`Low ±18°`, `Medium ±11°`, `High ±6°`).
- Game Speed settings (`Slow`, `Normal`, `Fast`).
- Toggle synthesized sound effects and haptic vibration.
- Reset high score with confirmation.

---

## 🛠 Technology Stack

- **Platform:** Android (minSdk 24, targetSdk 36)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose & Material 3
- **Architecture:** Clean MVVM (Model-View-ViewModel) + Coroutines & StateFlow
- **Sensors:** Android `SensorManager` (`Sensor.TYPE_ACCELEROMETER`)
- **Audio & Haptics:** Android `AudioTrack` PCM tone synthesis and `Vibrator` / `VibrationEffect`
- **Persistence:** Android `SharedPreferences` for game records and calculation history

---

## 🚀 Building & Running

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/calcsnake.git
   cd calcsnake
   ```
2. Build the project using Gradle:
   ```bash
   gradle assembleDebug
   ```
3. Run unit and Robolectric tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```

---

## 📱 Controls Summary

| Mode | Control Action | Effect |
| :--- | :--- | :--- |
| **Motion** | Tilt Phone Left / Right | Move Snake Left / Right |
| **Motion** | Tilt Phone Forward / Back | Move Snake Up / Down |
| **Motion** | Tap "Calibrate" | Zeroes current resting angle |
| **D-Pad** | On-screen Arrows | Manual Direction Input |
| **Calculator** | Keypad / Mode Tabs | Standard & Scientific fx |

---

## 📄 License
This project is open-source under the MIT License.
