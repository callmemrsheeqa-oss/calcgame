package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager {
    var isEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playClick() {
        if (!isEnabled) return
        scope.launch {
            playTone(frequency = 900.0, durationMs = 25, volume = 0.35f)
        }
    }

    fun playMove() {
        if (!isEnabled) return
        scope.launch {
            playTone(frequency = 320.0, durationMs = 20, volume = 0.2f)
        }
    }

    fun playEat() {
        if (!isEnabled) return
        scope.launch {
            playChirp(startFreq = 520.0, endFreq = 1040.0, durationMs = 80, volume = 0.5f)
        }
    }

    fun playBonusEat() {
        if (!isEnabled) return
        scope.launch {
            playChirp(startFreq = 700.0, endFreq = 1400.0, durationMs = 120, volume = 0.6f)
        }
    }

    fun playGameOver() {
        if (!isEnabled) return
        scope.launch {
            playChirp(startFreq = 380.0, endFreq = 130.0, durationMs = 250, volume = 0.6f)
        }
    }

    private suspend fun playTone(frequency: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val minBuff = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val numSamples = maxOf((sampleRate * (durationMs / 1000.0)).toInt(), 64)
            val buffer = ShortArray(numSamples)
            val angleIncrement = 2.0 * Math.PI * frequency / sampleRate
            var currentAngle = 0.0

            for (i in 0 until numSamples) {
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                    else -> 1.0
                }
                val sample = (sin(currentAngle) * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                currentAngle += angleIncrement
            }

            val bufferSizeBytes = maxOf(minBuff, buffer.size * 2)
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay(durationMs.toLong() + 30)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Throwable) {
            // Silently ignore audio playback errors
        }
    }

    private suspend fun playChirp(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val minBuff = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val numSamples = maxOf((sampleRate * (durationMs / 1000.0)).toInt(), 64)
            val buffer = ShortArray(numSamples)
            var currentAngle = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val angleIncrement = 2.0 * Math.PI * currentFreq / sampleRate

                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                    else -> 1.0
                }
                val sample = (sin(currentAngle) * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                currentAngle += angleIncrement
            }

            val bufferSizeBytes = maxOf(minBuff, buffer.size * 2)
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeBytes)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            delay(durationMs.toLong() + 30)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Throwable) {
            // Silently ignore audio playback errors
        }
    }
}
