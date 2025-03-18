package org.samo_lego.katara.tuner

import org.samo_lego.katara.util.HarmonicCorrections
import org.samo_lego.katara.util.InstrumentNotes
import org.samo_lego.katara.util.Note
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

private const val TOLERANCE_THRESHOLD = 10.0

fun processFrequency(frequency: Double): NoteData {
    // Calculate basic note data
    val noteData = calculateNoteData(frequency)

    // Apply harmonic correction if needed
    val correctedNoteData = HarmonicCorrections.correctHarmonicConfusion(noteData, frequency)

    // Find closest guitar string
    val closestString = InstrumentNotes.GUITAR_NOTES.findClosestString(frequency)

    if (closestString != null) {
        return calculateStringDifference(correctedNoteData, frequency, closestString)
    }
    return correctedNoteData
}


fun calculateStringDifference(noteData: NoteData, frequency: Double, closestString: NoteFrequency) : NoteData {
    // Calculate tuning information
    val centsDifference = calculateCentsDifference(frequency, closestString.frequency)

    // Determine tuning direction
    val tuningDirection = determineTuningDirection(centsDifference)

    // Return complete note data
    return noteData.copy(
        closestGuitarString = closestString,
        centsDifference = centsDifference,
        tuningDirection = tuningDirection
    )
}

/** Determine if note is in tune, too high, or too low */
fun determineTuningDirection(centsDifference: Double): TuningDirection {
    return when {
        abs(centsDifference) <= TOLERANCE_THRESHOLD -> TuningDirection.IN_TUNE
        centsDifference > 0 -> TuningDirection.TOO_HIGH
        else -> TuningDirection.TOO_LOW
    }
}

/** Calculate information about a note from its frequency */
fun calculateNoteData(frequency: Double): NoteData {
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
