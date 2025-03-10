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
    private val tag = "AudioInputStream"

    override fun getFormat(): TarsosDSPAudioFormat = format

    override fun skip(bytesToSkip: Long): Long {
        // Simple approach: read bytes and discard them
        var remainingBytes = bytesToSkip
        val buffer = ByteArray(1024)

        while (remainingBytes > 0) {
            val bytesToRead = minOf(buffer.size.toLong(), remainingBytes).toInt()
            val bytesSkipped = read(buffer, 0, bytesToRead)

            if (bytesSkipped <= 0) break

            remainingBytes -= bytesSkipped
        }

        return bytesToSkip - remainingBytes
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            Log.w(tag, "AudioRecord is not recording")
            return -1
        }

        // Read bytes directly into the buffer
        val bytesRead = audioRecord.read(buffer, offset, length)

        if (bytesRead > 0) {
            this.bytesRead += bytesRead
        } else if (bytesRead == 0) {
            Log.w(tag, "End of audio stream reached")
        } else {
            Log.e(tag, "Error reading audio data: $bytesRead")
        }

        return bytesRead
    }

    override fun close() {
        try {
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord.apply {
                    stop()
                    release()
                }
                Log.d(tag, "AudioInputStream closed successfully")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error closing AudioInputStream", e)
        }
    }

    override fun getFrameLength(): Long = -1 // Unknown frame length for streaming
}
