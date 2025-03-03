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

class TunerService() {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 1536
        private const val TAG = "TunerService"

        // Tolerance for determining if a string is in tune (in cents)
        private const val TUNING_TOLERANCE = 10.0

        // Standard reference frequency for A4
        private const val A4_FREQUENCY = 440.0

        // Amplitude threshold below which we consider the audio too soft to detect
        private const val AMPLITUDE_THRESHOLD = 0.01

        // Number of silent frames before resetting detection
        private const val SILENCE_RESET_THRESHOLD = 10

        // Maximum frequency difference in Hz to consider a note in the guitar's range
        private const val MAX_FREQUENCY_DIFFERENCE_PERCENT = 30.0

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
        private val STRING_BY_FREQUENCY = InstrumentType.GUITAR_STANDARD.strings.associateBy {
            GUITAR_FREQUENCIES[it.fullNoteName()] ?: 0.0
        }
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

    // Counter for consecutive silent frames
    private var silentFrameCount = 0

    // Last detected valid note
    private var lastValidNote: NoteData? = null

    /** Start the tuner service and begin detecting pitches */
    fun start() {
        Log.d(TAG, "Starting tuner")
        if (isRunning) return

        try {
            Log.d(TAG, "Creating audio dispatcher")
            _tunerState.value = TunerState.Starting

            // Create a new audio dispatcher
            val audioDispatcher =
                    AndroidAudioDispatcher.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP)

            // Create a pitch processor
            Log.d(TAG, "Creating pitch processor")
            val pitchProcessor = createPitchProcessor()

            // Add the pitch processor to the dispatcher
            Log.d(TAG, "Adding pitch processor to dispatcher")
            audioDispatcher.addAudioProcessor(pitchProcessor)

            // Start the dispatcher in a separate thread
            audioThread = Executors.newSingleThreadExecutor()
            dispatcher = audioDispatcher
            audioThread?.submit(audioDispatcher)
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
            dispatcher?.stop()
            audioThread?.shutdown()

            dispatcher = null
            audioThread = null
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
        val pitchHandler =
                PitchDetectionHandler { result: PitchDetectionResult, event: AudioEvent ->
                    val pitchInHz = result.pitch
                    val amplitude = event.rms

                    // Process the pitch on a background thread
                    serviceScope.launch {
                        if (amplitude < AMPLITUDE_THRESHOLD) {
                            silentFrameCount++

                            if (silentFrameCount >= SILENCE_RESET_THRESHOLD) {
                                // If we've had enough silent frames, clear the current note
                                if (_currentNoteData.value != null) {
                                    _currentNoteData.value = null
                                }
                            }
                        } else if (pitchInHz > 0 && result.probability > 0.85) {
                            // Reset silent frame count since we have a good signal
                            silentFrameCount = 0

                            // Process the detected frequency
                            val noteData = processFrequency(pitchInHz)

                            // Store as last valid note
                            lastValidNote = noteData

                            // Update current note data
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

        val adjustedNoteData = filterHarmonicConfusion(noteData, frequency.toDouble())

        // Find the closest guitar string
        val closestString = findClosestGuitarStringByFrequency(frequency.toDouble())

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

        return adjustedNoteData.copy(
                closestGuitarString = closestString,
                centsDifference = centsDifference,
                tuningDirection = tuningDirection
        )
    }

    /**
     * Filter out common harmonic confusion cases
     * This helps prevent E4 from being misidentified as A2, etc.
     */
    private fun filterHarmonicConfusion(noteData: NoteData, frequency: Double): NoteData {
         val fullNote = noteData.fullNoteName

         // Handle octave confusion for D notes
         if (fullNote == "D4" && frequency < 180.0) {
             Log.d(TAG, "Correcting octave confusion: D4 -> D3 ($frequency Hz)")
             return calculateNoteData(146.83) // Use D3 frequency
         }

         // Handle octave confusion for A notes
         if (fullNote == "A3" && frequency < 130.0) {
             Log.d(TAG, "Correcting octave confusion: A3 -> A2 ($frequency Hz)")
             return calculateNoteData(110.0) // Use A2 frequency
         }

         // Handle octave confusion for E notes
         if (fullNote == "E3" && frequency < 100.0) {
             Log.d(TAG, "Correcting octave confusion: E3 -> E2 ($frequency Hz)")
             return calculateNoteData(82.41) // Use E2 frequency
         }

         // Handle E4 detected as A2 (3rd harmonic)
         if (fullNote == "A2" && frequency > 300.0 && frequency < 340.0) {
             Log.d(TAG, "Correcting harmonic confusion: A2 -> E4 ($frequency Hz)")
             return calculateNoteData(329.63) // Use E4 frequency
         }

         // Handle B3 detected as E2 (3rd harmonic)
         if (fullNote == "E2" && frequency > 230.0 && frequency < 260.0) {
             Log.d(TAG, "Correcting harmonic confusion: E2 -> B3 ($frequency Hz)")
             return calculateNoteData(246.94) // Use B3 frequency
         }

         return noteData
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

    /**
     * Find closest guitar string by comparing actual frequencies.
     * This avoids octave confusion issues
     */
    private fun findClosestGuitarStringByFrequency(frequency: Double): InstrumentString? {
        var closestString: InstrumentString? = null
        var minPercentDifference = Double.MAX_VALUE

        // Compare to each standard guitar string frequency
        for ((stringFreq, instrumentString) in STRING_BY_FREQUENCY) {
            if (stringFreq <= 0.0) continue

            // Calculate percentage difference to handle different ranges better
            val percentDiff = abs((frequency - stringFreq) / stringFreq) * 100.0

            if (percentDiff < minPercentDifference) {
                minPercentDifference = percentDiff
                closestString = instrumentString
            }
        }

        // Only accept matches within reasonable percentage difference
        return if (minPercentDifference <= MAX_FREQUENCY_DIFFERENCE_PERCENT) {
            Log.d(TAG, "Matched to ${closestString?.fullNoteName()} with $minPercentDifference% difference")
            closestString
        } else {
            Log.d(TAG, "No close match found for ${frequency}Hz (closest: ${closestString?.fullNoteName()} with $minPercentDifference% difference)")
            null
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
