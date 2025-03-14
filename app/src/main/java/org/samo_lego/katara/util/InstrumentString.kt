package org.samo_lego.katara.util

import kotlin.math.abs
import org.samo_lego.katara.tuner.NoteData

enum class Note(val noteName: String) {
    A("A"),
    A_SHARP("A#"),
    B("B"),
    C("C"),
    C_SHARP("C#"),
    D("D"),
    D_SHARP("D#"),
    E("E"),
    F("F"),
    F_SHARP("F#"),
    G("G"),
    G_SHARP("G#");

    companion object {
        private val values = entries.toTypedArray()

        // Get note by index (0-11)
        fun fromIndex(index: Int): Note = values[index % 12]

        // Get notes array in order for calculations
        fun getOrderedNotes(): Array<Note> =
                arrayOf(C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B)
    }
}

enum class NoteFrequency(val note: Note, val frequency: Double, val octave: Int) {
    E4(Note.E, 329.63, 4),
    E2(Note.E, 82.41, 2),
    B3(Note.B, 246.94, 3),
    G3(Note.G, 196.0, 3),
    G4(Note.G, 392.00, 4),
    D3(Note.D, 146.83, 3),
    C4(Note.C, 261.63, 4),
    A2(Note.A, 110.0, 2),
    A4(Note.A, 440.0, 4);

    val fullNoteName: String
        get() = "${note.noteName}$octave"
}

enum class InstrumentNotes(val notes: Array<NoteFrequency>) {
    GUITAR_NOTES(
            arrayOf(
                    NoteFrequency.E4,
                    NoteFrequency.B3,
                    NoteFrequency.G3,
                    NoteFrequency.D3,
                    NoteFrequency.A2,
                    NoteFrequency.E2
            )
    ),
    UKULELE_NOTES(
            arrayOf(
                    NoteFrequency.A4,
                    NoteFrequency.E4,
                    NoteFrequency.C4,
                    NoteFrequency.G4,
            )
    );

    /** Find the closest string to the given frequency */
    fun findClosestString(frequency: Double, maxPercentDifference: Double = 30.0): NoteFrequency? {
        var closestNote: NoteFrequency? = null
        var minPercentDifference = Double.MAX_VALUE

        for (note in notes) {
            val noteFreq = note.frequency
            val percentDiff = abs((frequency - noteFreq) / noteFreq) * 100.0

            if (percentDiff < minPercentDifference) {
                minPercentDifference = percentDiff
                closestNote = note
            }
        }

        return if (minPercentDifference <= maxPercentDifference) closestNote else null
    }

    /** Get string number (1-indexed) for a note frequency in this instrument */
    fun getStringNumber(noteFreq: NoteFrequency): Int {
        return notes.indexOf(noteFreq) + 1
    }
}

/** Helper object for correcting harmonic confusion in pitch detection */
object HarmonicCorrections {
    // Sealed classes for correction types
    sealed class Correction {
        abstract fun appliesTo(note: Note, octave: Int, frequency: Double): Boolean
        abstract val targetNoteFreq: NoteFrequency
    }

    class HarmonicCorrection(
            val note: Note,
            val octave: Int,
            val thresholdFreq: Double,
            override val targetNoteFreq: NoteFrequency
    ) : Correction() {
        override fun appliesTo(note: Note, octave: Int, frequency: Double): Boolean {
            return this.note == note && this.octave == octave && frequency < thresholdFreq
        }
    }

    class RangeCorrection(
            val note: Note,
            val octave: Int,
            val minFreq: Double,
            val maxFreq: Double,
            override val targetNoteFreq: NoteFrequency
    ) : Correction() {
        override fun appliesTo(note: Note, octave: Int, frequency: Double): Boolean {
            return this.note == note &&
                    this.octave == octave &&
                    frequency > minFreq &&
                    frequency < maxFreq
        }
    }

    // Common harmonic confusion cases
    private val corrections =
            listOf(
                    HarmonicCorrection(Note.D, 4, 180.0, NoteFrequency.D3),
                    HarmonicCorrection(Note.A, 3, 130.0, NoteFrequency.A2),
                    HarmonicCorrection(
                            Note.E,
                            3,
                            165.0,
                            NoteFrequency.E2
                    ), // E3 -> E2 if below 165 Hz
                    RangeCorrection(Note.A, 2, 300.0, 340.0, NoteFrequency.E4),
                    RangeCorrection(Note.E, 2, 230.0, 260.0, NoteFrequency.B3)
            )

    /** Correct common harmonic confusion issues */
    fun correctHarmonicConfusion(noteData: NoteData, frequency: Double): NoteData {
        val noteObj = Note.entries.find { it.noteName == noteData.noteName }

        // If we can't match the note, just return the original data
        noteObj?.let { note ->
            // Find applicable correction
            for (correction in corrections) {
                if (correction.appliesTo(note, noteData.octave, frequency)) {
                    return noteData.copy(
                            noteName = correction.targetNoteFreq.note.noteName,
                            octave = correction.targetNoteFreq.octave
                    )
                }
            }
        }

        return noteData
    }
}
