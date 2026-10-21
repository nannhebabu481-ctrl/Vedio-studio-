package com.example.engine

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.sin

object DemoMediaGenerator {

    /**
     * Generates a playable WAV audio file with musical chords and rhythm.
     */
    fun getOrCreateBgmFile(context: Context, trackId: String, bpm: Int = 100, durationSec: Int = 15): File {
        val audioDir = File(context.filesDir, "studio_bgm").apply { mkdirs() }
        val targetFile = File(audioDir, "$trackId.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return targetFile
        }

        generateMusicalWav(targetFile, trackId, bpm, durationSec)
        return targetFile
    }

    /**
     * Generates a sample dialogue/ambient video audio track for testing.
     */
    fun getOrCreateSampleVideoAudio(context: Context): File {
        val audioDir = File(context.filesDir, "studio_media").apply { mkdirs() }
        val targetFile = File(audioDir, "demo_video_audio.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return targetFile
        }

        generateDialogueWav(targetFile, durationSec = 16)
        return targetFile
    }

    private fun generateMusicalWav(file: File, style: String, bpm: Int, durationSec: Int) {
        val sampleRate = 44100
        val totalSamples = sampleRate * durationSec
        val bytesPerSample = 2
        val numChannels = 2
        val subChunk2Size = totalSamples * numChannels * bytesPerSample

        val baseFrequencies = when (style) {
            "bgm_cinematic" -> doubleArrayOf(130.81, 196.00, 261.63, 329.63, 392.00) // C minor / major pad
            "bgm_synthwave" -> doubleArrayOf(110.00, 220.00, 277.18, 329.63, 440.00) // A minor synth
            "bgm_lofi" -> doubleArrayOf(146.83, 174.61, 220.00, 261.63, 329.63) // Dm9 chord
            "bgm_acoustic" -> doubleArrayOf(196.00, 246.94, 293.66, 392.00, 493.88) // G major acoustic
            "bgm_bass" -> doubleArrayOf(55.00, 82.41, 110.00, 164.81) // Deep Sub Bass
            else -> doubleArrayOf(130.81, 164.81, 196.00, 246.94, 261.63) // Cmaj7 ambient
        }

        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, numChannels, sampleRate, bytesPerSample * 8, subChunk2Size)

            val beatInterval = (sampleRate * 60.0 / bpm).toInt()
            val buffer = ByteArray(1024)
            var bufIdx = 0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val beatPos = i % beatInterval
                val beatDecay = (1.0 - (beatPos.toDouble() / beatInterval)).coerceIn(0.0, 1.0)

                // Harmonic synthesis
                var leftSample = 0.0
                var rightSample = 0.0

                for ((idx, freq) in baseFrequencies.withIndex()) {
                    val detune = 1.0 + (idx * 0.002)
                    val noteEnv = 0.25 / (idx + 1)
                    val wave = sin(2.0 * Math.PI * freq * detune * t)
                    leftSample += wave * noteEnv
                    rightSample += sin(2.0 * Math.PI * (freq * 1.003) * t) * noteEnv
                }

                // Add gentle kick/rhythm pulse on downbeats
                if (beatPos < sampleRate * 0.1) {
                    val kickFreq = 120.0 * (1.0 - (beatPos.toDouble() / (sampleRate * 0.1)))
                    val kick = sin(2.0 * Math.PI * kickFreq * t) * 0.35 * beatDecay
                    leftSample += kick
                    rightSample += kick
                }

                // Master volume & soft limit
                val leftShort = (leftSample.coerceIn(-0.95, 0.95) * 32767).toInt().toShort()
                val rightShort = (rightSample.coerceIn(-0.95, 0.95) * 32767).toInt().toShort()

                // Little endian 16-bit
                buffer[bufIdx++] = (leftShort.toInt() and 0xFF).toByte()
                buffer[bufIdx++] = ((leftShort.toInt() shr 8) and 0xFF).toByte()
                buffer[bufIdx++] = (rightShort.toInt() and 0xFF).toByte()
                buffer[bufIdx++] = ((rightShort.toInt() shr 8) and 0xFF).toByte()

                if (bufIdx >= buffer.size) {
                    fos.write(buffer, 0, bufIdx)
                    bufIdx = 0
                }
            }

            if (bufIdx > 0) {
                fos.write(buffer, 0, bufIdx)
            }
        }
    }

    private fun generateDialogueWav(file: File, durationSec: Int) {
        val sampleRate = 44100
        val totalSamples = sampleRate * durationSec
        val bytesPerSample = 2
        val numChannels = 1
        val subChunk2Size = totalSamples * numChannels * bytesPerSample

        FileOutputStream(file).use { fos ->
            writeWavHeader(fos, numChannels, sampleRate, bytesPerSample * 8, subChunk2Size)

            val buffer = ByteArray(1024)
            var bufIdx = 0

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                // Speech cadence: talk for 1.8 sec, pause 0.8 sec, talk for 2.2 sec
                val speechCycle = t % 4.0
                val isSpeaking = speechCycle < 2.2

                var sample = 0.0
                if (isSpeaking) {
                    // Formant simulation around 180Hz, 800Hz, 2400Hz
                    val f0 = 180.0 + 25.0 * sin(2.0 * Math.PI * 3.5 * t)
                    val formant1 = sin(2.0 * Math.PI * f0 * t) * 0.35
                    val formant2 = sin(2.0 * Math.PI * (f0 * 4.2) * t) * 0.20
                    val formant3 = sin(2.0 * Math.PI * (f0 * 8.5) * t) * 0.10
                    sample = (formant1 + formant2 + formant3)
                } else {
                    // Subtle room tone / mic noise
                    sample = (Math.random() - 0.5) * 0.02
                }

                val sampleShort = (sample.coerceIn(-0.95, 0.95) * 32767).toInt().toShort()
                buffer[bufIdx++] = (sampleShort.toInt() and 0xFF).toByte()
                buffer[bufIdx++] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()

                if (bufIdx >= buffer.size) {
                    fos.write(buffer, 0, bufIdx)
                    bufIdx = 0
                }
            }

            if (bufIdx > 0) {
                fos.write(buffer, 0, bufIdx)
            }
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        channels: Int,
        sampleRate: Int,
        bitsPerSample: Int,
        dataLength: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()
        val totalLength = dataLength + 36

        val header = ByteArray(44)
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalLength and 0xFF).toByte()
        header[5] = ((totalLength shr 8) and 0xFF).toByte()
        header[6] = ((totalLength shr 16) and 0xFF).toByte()
        header[7] = ((totalLength shr 24) and 0xFF).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // SubChunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat (1 for PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = ((sampleRate shr 8) and 0xFF).toByte()
        header[26] = ((sampleRate shr 16) and 0xFF).toByte()
        header[27] = ((sampleRate shr 24) and 0xFF).toByte()
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = ((byteRate shr 8) and 0xFF).toByte()
        header[30] = ((byteRate shr 16) and 0xFF).toByte()
        header[31] = ((byteRate shr 24) and 0xFF).toByte()
        header[32] = (blockAlign.toInt() and 0xFF).toByte()
        header[33] = ((blockAlign.toInt() shr 8) and 0xFF).toByte()
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (dataLength and 0xFF).toByte()
        header[41] = ((dataLength shr 8) and 0xFF).toByte()
        header[42] = ((dataLength shr 16) and 0xFF).toByte()
        header[43] = ((dataLength shr 24) and 0xFF).toByte()

        out.write(header, 0, 44)
    }
}
