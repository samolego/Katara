package org.samo_lego.katara.audio

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
        // Audio format configuration
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val channelConfig = AudioFormat.CHANNEL_IN_MONO

        // Calculate buffer size (use at least the minimum required size)
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val recordBufferSize = maxOf(bufferSize, minBufferSize)

        // Create and start recording
        val audioRecord =
                AudioRecord(
                                MediaRecorder.AudioSource.MIC,
                                sampleRate,
                                channelConfig,
                                audioFormat,
                                recordBufferSize
                        )
                        .apply { startRecording() }

        // Create TarsosDSP format (16-bit, mono, little-endian)
        val tarsosDSPFormat = TarsosDSPAudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val audioStream = AudioInputStream(audioRecord, tarsosDSPFormat)

        // Create and return the dispatcher
        return AudioDispatcher(audioStream, bufferSize, bufferOverlap)
    }
}
