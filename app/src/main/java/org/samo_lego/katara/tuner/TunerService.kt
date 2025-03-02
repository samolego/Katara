package org.samo_lego.katara.tuner

import android.content.Context
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.InstrumentType
import org.samo_lego.katara.util.TuningDirection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class TunerService(private val context: Context) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1536
        private const val TAG = "TunerService"

        // Tolerance for determining if a string is in tune (in cents)
        private const val TUNING_TOLERANCE = 10.0

        // Standard reference frequency for A4
        private const val A4_FREQUENCY = 440.0

        // Standard guitar string frequencies (for reference)
        private val GUITAR_FREQUENCIES =
                mapOf(
                        "E2" to 82.41,
                        "A2" to 110.0,
                        "D3" to 146.83,
                        "G3" to 196.0,
                        "B3" to 246.94,
                        "E4" to 329.63
                )
    }

    private val _tunerState = MutableStateFlow<TunerState>(TunerState.Inactive)
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var audioThread: ExecutorService? = null
    private var dispatcher: AudioDispatcher? = null
    private var isRunning = false

    // Storage for the currently detected note information
    private val _currentNoteData = MutableStateFlow<NoteData?>(null)
    val currentNoteData: StateFlow<NoteData?> = _currentNoteData.asStateFlow()

    /** Start the tuner service and begin detecting pitches */
    fun start() {
        Log.d(TAG, "Starting tuner")
        if (isRunning) return

        try {
            Log.d(TAG, "Creating audio dispatcher")
            _tunerState.value = TunerState.Starting

            // Create a new audio dispatcher
            dispatcher =
                    AndroidAudioDispatcher.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP)

            // Create a pitch processor
            Log.d(TAG, "Creating pitch processor")
            val pitchProcessor = createPitchProcessor()

            // Add the pitch processor to the dispatcher
            Log.d(TAG, "Adding pitch processor to dispatcher")
            dispatcher?.addAudioProcessor(pitchProcessor)

            // Start the dispatcher in a separate thread
            audioThread = Executors.newSingleThreadExecutor()
            dispatcher?.let {
                audioThread?.submit(it)
                isRunning = true
                _tunerState.value = TunerState.Active
            }
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
            dispatcher?.stop()
            audioThread?.shutdown()

            dispatcher = null
            audioThread = null
            isRunning = false

            _tunerState.value = TunerState.Inactive
            _currentNoteData.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tuner", e)
            _tunerState.value = TunerState.Error(e.message ?: "Unknown error stopping tuner")
        }
    }

    /** Create a pitch processor for detecting notes */
    private fun createPitchProcessor(): AudioProcessor {
        val pitchHandler =
                PitchDetectionHandler { result: PitchDetectionResult, event: AudioEvent ->
                    val pitchInHz = result.pitch

                    // Process the pitch on a background thread
                    serviceScope.launch {
                        if (pitchInHz > 0 && result.probability > 0.85) {
                            val noteData = processFrequency(pitchInHz)
                            _currentNoteData.value = noteData
                        }
                    }
                }

        return PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                SAMPLE_RATE.toFloat(),
                BUFFER_SIZE,
                pitchHandler
        )
    }

    /** Process a detected frequency to determine note information */
    private fun processFrequency(frequency: Float): NoteData {
        // Calculate note details from the frequency
        val noteData = calculateNoteData(frequency.toDouble())

        // Find the closest guitar string
        val closestString = findClosestGuitarString(noteData.noteName, noteData.octave)

        // Calculate how far the detected note is from the target (in cents)
        val centsDifference =
                if (closestString != null) {
                    val targetFreq = GUITAR_FREQUENCIES[closestString.fullNoteName()] ?: 0.0
                    calculateCentsDifference(frequency.toDouble(), targetFreq)
                } else {
                    0.0
                }

        // Determine tuning direction
        val tuningDirection =
                when {
                    abs(centsDifference) <= TUNING_TOLERANCE -> TuningDirection.IN_TUNE
                    centsDifference > 0 ->
                            TuningDirection.TOO_HIGH // Frequency is higher than target
                    else -> TuningDirection.TOO_LOW // Frequency is lower than target
                }

        return noteData.copy(
                closestGuitarString = closestString,
                centsDifference = centsDifference,
                tuningDirection = tuningDirection
        )
    }

    /** Calculate information about a note from its frequency */
    private fun calculateNoteData(frequency: Double): NoteData {
        // A4 is 440Hz, which is 69 semitones above C0
        val semitoneFromA4 = 12 * log2(frequency / A4_FREQUENCY)
        val midiNote = (69 + semitoneFromA4).roundToInt()

        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteName = noteNames[midiNote % 12]
        val octave = (midiNote / 12) - 1

        // Calculate exact frequency for this note
        val exactFrequency = A4_FREQUENCY * 2.0.pow((midiNote - 69) / 12.0)

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

    /** Find the closest standard guitar string to the detected note */
    private fun findClosestGuitarString(noteName: String, octave: Int): InstrumentString? {
        // First try exact match
        val fullNoteName = "$noteName$octave"

        // Get the standard guitar strings
        val guitarStrings = InstrumentType.GUITAR_STANDARD.strings

        // Look for exact match
        for (instrumentString in guitarStrings) {
            if (instrumentString.fullNoteName() == fullNoteName) {
                return instrumentString
            }
        }

        // If no exact match, find closest
        return when (fullNoteName) {
            // Handle common cases where the note is between standard guitar strings
            "F2" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(6) // E2
            "G2", "G#2" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(5) // A2
            "A#2", "C3" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(4) // D3
            "D#3", "F3" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(3) // G3
            "A3" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(2) // B3
            "C4", "D4" -> InstrumentType.GUITAR_STANDARD.getStringByNumber(1) // E4
            else -> null // Not close to any standard guitar string
        }
    }

    /** Calculate the difference in cents between two frequencies */
    private fun calculateCentsDifference(detectedFreq: Double, targetFreq: Double): Double {
        return 1200 * log2(detectedFreq / targetFreq)
    }
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
        val frequency: Double, // Detected frequency in Hz
        val midiNote: Int, // MIDI note number
        val noteName: String, // Note name (C, C#, D, etc.)
        val octave: Int, // Octave number
        val cents: Double, // Cents off from exact note (-50 to +50)
        val closestGuitarString: InstrumentString?, // Closest guitar string
        val centsDifference: Double, // Cents difference from guitar string
        val tuningDirection: TuningDirection // Direction to tune
) {
    val fullNoteName: String
        get() = "$noteName$octave"
}
