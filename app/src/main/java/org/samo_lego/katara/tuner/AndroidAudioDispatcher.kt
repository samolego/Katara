package org.samo_lego.katara.tuner

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.TarsosDSPAudioFormat

object AndroidAudioDispatcher {
    /** Create an audio dispatcher using the default microphone */
    @SuppressLint("MissingPermission")
    fun fromDefaultMicrophone(
            sampleRate: Int,
            bufferSize: Int,
            bufferOverlap: Int
    ): AudioDispatcher {
        // Configure AudioRecord
        val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
        val channelConfig = AudioFormat.CHANNEL_IN_MONO

        // Calculate minimum buffer size required
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        // Use larger buffer to avoid underruns
        val recordBufferSize = if (bufferSize < minBufferSize) minBufferSize else bufferSize

        // Create the AudioRecord instance
        val audioRecord =
                AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        recordBufferSize * 2 // Double buffer for safety
                )

        // Start recording
        audioRecord.startRecording()

        // Create TarsosDSP compatible format
        val tarsosDSPFormat = TarsosDSPAudioFormat(sampleRate.toFloat(), 16, 1, true, false)

        // Create input stream wrapper
        val audioStream = AudioInputStream(audioRecord, tarsosDSPFormat)

        // Create and return the dispatcher
        return AudioDispatcher(audioStream, bufferSize, bufferOverlap)
    }
}
