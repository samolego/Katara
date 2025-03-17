package org.samo_lego.katara.tuner

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.samo_lego.katara.util.HarmonicCorrections
import org.samo_lego.katara.util.InstrumentNotes
import org.samo_lego.katara.util.Note
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection

class TunerService {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1536
        private const val TAG = "TunerService"

        private const val TUNING_TOLERANCE = 10.0
        private const val AMPLITUDE_THRESHOLD = 0.01
        private const val SILENCE_RESET_THRESHOLD = 10
    }

    private val _tunerServiceState = MutableStateFlow<TunerServiceState>(TunerServiceState.Inactive)
    val tunerServiceState: StateFlow<TunerServiceState> = _tunerServiceState.asStateFlow()

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
            _tunerServiceState.value = TunerServiceState.Starting

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
                            _tunerServiceState.value =
                                    TunerServiceState.Error("Audio processing error: ${e.message}")
                        }
                    }

            isRunning = true
            _tunerServiceState.value = TunerServiceState.Active
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tuner", e)
            _tunerServiceState.value = TunerServiceState.Error(e.message ?: "Unknown error")
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

            _tunerServiceState.value = TunerServiceState.Inactive
            _currentNoteData.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tuner", e)
            _tunerServiceState.value = TunerServiceState.Error(e.message ?: "Unknown error stopping tuner")
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
    private fun processFrequency(frequency: Double): NoteData {
        // Calculate basic note data
        val noteData = calculateNoteData(frequency)

        // Apply harmonic correction if needed
        val correctedNoteData = HarmonicCorrections.correctHarmonicConfusion(noteData, frequency)

        // Find closest guitar string
        val closestString = InstrumentNotes.GUITAR_NOTES.findClosestString(frequency)

        // Calculate tuning information
        val centsDifference =
                closestString?.let { calculateCentsDifference(frequency, it.frequency) } ?: 0.0

        // Determine tuning direction
        val tuningDirection = determineTuningDirection(centsDifference)

        // Return complete note data
        return correctedNoteData.copy(
                closestGuitarString = closestString,
                centsDifference = centsDifference,
                tuningDirection = tuningDirection
        )
    }

    /** Determine if note is in tune, too high, or too low */
    private fun determineTuningDirection(centsDifference: Double): TuningDirection {
        return when {
            abs(centsDifference) <= TUNING_TOLERANCE -> TuningDirection.IN_TUNE
            centsDifference > 0 -> TuningDirection.TOO_HIGH
            else -> TuningDirection.TOO_LOW
        }
    }

    /** Calculate information about a note from its frequency */
    private fun calculateNoteData(frequency: Double): NoteData {
        val semitoneFromA4 = 12 * log2(frequency / NoteFrequency.A4.frequency)
        val midiNote = (69 + semitoneFromA4).roundToInt()

        // Get note and octave from MIDI note
        val noteIndex = (midiNote % 12)
        val noteName = Note.getOrderedNotes()[noteIndex].noteName
        val octave = (midiNote / 12) - 1

        // Calculate exact frequency for this note
        val exactFrequency = NoteFrequency.A4.frequency * 2.0.pow((midiNote - 69) / 12.0)

        // Calculate cents off from the exact note frequency
        val cents = 1200 * log2(frequency / exactFrequency)

        return NoteData(
                frequency = frequency,
                midiNote = midiNote,
                noteName = noteName,
                octave = octave,
                cents = cents,
                closestGuitarString = null,
                centsDifference = 0.0,
                tuningDirection = TuningDirection.IN_TUNE
        )
    }

    /** Calculate the difference in cents between two frequencies */
    private fun calculateCentsDifference(detectedFreq: Double, targetFreq: Double): Double {
        return 1200 * log2(detectedFreq / targetFreq)
    }
}

/** Represents the current state of the tuner */
sealed class TunerServiceState {
    object Inactive : TunerServiceState()
    object Starting : TunerServiceState()
    object Active : TunerServiceState()
    data class Error(val message: String) : TunerServiceState()
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
