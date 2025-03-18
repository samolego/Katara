package org.samo_lego.katara.tuner

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
                    RangeCorrection(Note.E, 2, 230.0, 260.0, NoteFrequency.B3),
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