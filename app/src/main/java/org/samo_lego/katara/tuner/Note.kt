package org.samo_lego.katara.tuner

import kotlin.math.abs

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

    companion object {
        /** Find the closest string to the given frequency */
        fun findClosestString(frequency: Double, maxPercentDifference: Double = 30.0): NoteFrequency? {
            var closestNote: NoteFrequency? = null
            var minPercentDifference = Double.MAX_VALUE

            for (note in entries) {
                val noteFreq = note.frequency
                val percentDiff = abs((frequency - noteFreq) / noteFreq) * 100.0

                if (percentDiff < minPercentDifference) {
                    minPercentDifference = percentDiff
                    closestNote = note
                }
            }

            return if (minPercentDifference <= maxPercentDifference) closestNote else null
        }
    }
}
