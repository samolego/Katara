package org.samo_lego.katara.tuner

import android.media.AudioRecord
import android.util.Log
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream

/** Implementation of TarsosDSPAudioInputStream that works with Android's AudioRecord */
class AudioInputStream(
        private val audioRecord: AudioRecord,
        private val format: TarsosDSPAudioFormat
) : TarsosDSPAudioInputStream {

    private var bytesRead = 0L
    private val TAG = "AudioInputStream"

    override fun getFormat(): TarsosDSPAudioFormat = format

    override fun skip(bytesToSkip: Long): Long {
        var totalBytesSkipped = 0L
        val buffer = ByteArray(1024)

        while (totalBytesSkipped < bytesToSkip) {
            val bytesRemaining = bytesToSkip - totalBytesSkipped
            val bytesToRead = minOf(buffer.size.toLong(), bytesRemaining).toInt()

            val bytesSkipped = read(buffer, 0, bytesToRead)
            if (bytesSkipped <= 0) {
                break
            }

            totalBytesSkipped += bytesSkipped
        }

        return totalBytesSkipped
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            Log.w(TAG, "AudioRecord is not recording")
            return -1
        }

        // TarsosDSP expects 16-bit PCM data, but we're reading float samples
        // We need to read floats and convert to 16-bit PCM

        // Determine how many float samples we need to read
        val floatSamples = length / 2 // 2 bytes per 16-bit sample
        val floatBuffer = FloatArray(floatSamples)

        // Read float samples from AudioRecord
        val samplesRead = audioRecord.read(floatBuffer, 0, floatSamples, AudioRecord.READ_BLOCKING)

        if (samplesRead <= 0) {
            return samplesRead
        }

        // Convert float samples to 16-bit PCM
        for (i in 0 until samplesRead) {
            // Convert float in range [-1.0, 1.0] to 16-bit PCM
            val shortSample = (floatBuffer[i] * 32767f).toInt().coerceIn(-32768, 32767).toShort()

            // Write the 16-bit sample to the byte buffer (little endian)
            val byteOffset = offset + i * 2
            buffer[byteOffset] = (shortSample.toInt() and 0xFF).toByte()
            buffer[byteOffset + 1] = ((shortSample.toInt() shr 8) and 0xFF).toByte()
        }

        bytesRead += samplesRead * 2

        // Return the number of bytes written
        return samplesRead * 2
    }

    override fun close() {
        Log.d("AudioInputStream", "Closing AudioInputStream")
        try {
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop()
                audioRecord.release()
            }
        } catch (_: Exception) {
            // Ignore exceptions during close
        }
    }

    override fun getFrameLength(): Long = -1 // Unknown frame length for streaming
}
