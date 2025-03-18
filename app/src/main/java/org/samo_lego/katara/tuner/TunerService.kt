package org.samo_lego.katara.tuner

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TunerService {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1536
        private const val TAG = "TunerService"

        private const val AMPLITUDE_THRESHOLD = 0.01
        private const val SILENCE_RESET_THRESHOLD = 15
    }

    private val _tunerState = MutableStateFlow<TunerState>(TunerState.Inactive)
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var audioJob: Job? = null
    private var dispatcher: AudioDispatcher? = null
    private var isRunning = false

    private val _currentNoteData = MutableStateFlow<NoteData?>(null)
    val currentNoteData: StateFlow<NoteData?> = _currentNoteData.asStateFlow()

    private var silentFrameCount = 0
    private var lastValidNote: NoteData? = null

    /** Start the tuner service and begin detecting pitches */
    fun start() {
        Log.d(TAG, "Starting tuner")
        if (isRunning) return

        try {
            _tunerState.value = TunerState.Starting

            // Create and configure audio dispatcher
            val audioDispatcher =
                    AndroidAudioDispatcher.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP)
                            .apply { addAudioProcessor(createPitchProcessor()) }

            // Start processing in a background coroutine
            dispatcher = audioDispatcher
            audioJob =
                    serviceScope.launch {
                        try {
                            audioDispatcher.run()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in audio dispatcher", e)
                            _tunerState.value =
                                    TunerState.Error("Audio processing error: ${e.message}")
                        }
                    }

            isRunning = true
            _tunerState.value = TunerState.Active
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tuner", e)
            _tunerState.value = TunerState.Error(e.message ?: "Unknown error")
            stop()
        }
    }

    /** Stop the tuner service */
    fun stop() {
        if (!isRunning) return

        try {
            // Stop the dispatcher (which will terminate the coroutine)
            dispatcher?.stop()

            // Cancel the coroutine job if still active
            audioJob?.cancel()

            dispatcher = null
            audioJob = null
            isRunning = false
            silentFrameCount = 0
            lastValidNote = null

            _tunerState.value = TunerState.Inactive
            _currentNoteData.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tuner", e)
            _tunerState.value = TunerState.Error(e.message ?: "Unknown error stopping tuner")
        }
    }

    /** Create a pitch processor for detecting notes */
    private fun createPitchProcessor(): AudioProcessor {
        return PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                SAMPLE_RATE.toFloat(),
                BUFFER_SIZE,
                PitchDetectionHandler { result, event -> handlePitchDetection(result, event) }
        )
    }

    /** Handle pitch detection results */
    private fun handlePitchDetection(result: PitchDetectionResult, event: AudioEvent) {
        val pitchInHz = result.pitch
        val amplitude = event.rms

        // Process on background thread
        serviceScope.launch {
            when {
                // Handle silence
                amplitude < AMPLITUDE_THRESHOLD -> handleSilence()

                // Handle valid pitch
                pitchInHz > 0 && result.probability > 0.85 -> handleValidPitch(pitchInHz)
            }
        }
    }

    /** Handle silence (no detectable pitch) */
    private fun handleSilence() {
        silentFrameCount++
        if (silentFrameCount >= SILENCE_RESET_THRESHOLD && _currentNoteData.value != null) {
            _currentNoteData.value = null
        }
    }

    /** Handle a valid pitch detection */
    private fun handleValidPitch(frequencyHz: Float) {
        silentFrameCount = 0
        val noteData = processFrequency(frequencyHz.toDouble())
        lastValidNote = noteData
        _currentNoteData.value = noteData
    }

    /** Process a detected frequency to determine note information */

}

/** Represents the current state of the tuner */
sealed class TunerState {
    object Inactive : TunerState()
    object Starting : TunerState()
    object Active : TunerState()
    data class Error(val message: String) : TunerState()
}

/** Contains information about a detected note */
data class NoteData(
        val frequency: Double,
        val midiNote: Int,
        val noteName: String,
        val octave: Int,
        val cents: Double,
        val closestGuitarString: NoteFrequency?,
        val centsDifference: Double,
        val tuningDirection: TuningDirection
) {
    val fullNoteName: String
        get() = "$noteName$octave"
}
