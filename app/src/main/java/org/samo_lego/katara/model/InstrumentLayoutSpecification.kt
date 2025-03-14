package org.samo_lego.katara.model

import org.samo_lego.katara.R
import org.samo_lego.katara.util.InstrumentNotes
import org.samo_lego.katara.util.NoteFrequency

/** Represents specifications for a guitar type, including string and tuner positions. */
data class InstrumentLayoutSpecification(
        val instrumentNotes: InstrumentNotes,
        val viewportWidth: Float,
        val viewportHeight: Float,
        val stringPositions: Map<NoteFrequency, StringPosition>,
        val bottomStringY: Float,
        val knobsXOffsets: Map<NoteFrequency, Float>,
        val drawableId: Int,

        ) {
    companion object {
        val GUITAR_STANDARD =
                InstrumentLayoutSpecification(
                        instrumentNotes = InstrumentNotes.GUITAR_NOTES,
                        viewportWidth = 153f,
                        viewportHeight = 326f,
                        bottomStringY = 297f,
                        knobsXOffsets =
                                mapOf(
                                        InstrumentNotes.GUITAR_NOTES.notes[3] to -50f,
                                        InstrumentNotes.GUITAR_NOTES.notes[4] to -50f,
                                        InstrumentNotes.GUITAR_NOTES.notes[5] to -50f,
                                        InstrumentNotes.GUITAR_NOTES.notes[2] to 50f,
                                        InstrumentNotes.GUITAR_NOTES.notes[1] to 50f,
                                        InstrumentNotes.GUITAR_NOTES.notes[0] to 50f,
                                ),
                        stringPositions =
                                mapOf(
                                        // Left side (low to high)
                                        InstrumentNotes.GUITAR_NOTES.notes[3] to
                                                StringPosition(36f, 63f, 69f),
                                        InstrumentNotes.GUITAR_NOTES.notes[4] to
                                                StringPosition(36f, 143f, 57f),
                                        InstrumentNotes.GUITAR_NOTES.notes[5] to
                                                StringPosition(36f, 223f, 47f),
                                        // Right side (low to high)
                                        InstrumentNotes.GUITAR_NOTES.notes[2] to
                                                StringPosition(116f, 63f, 83f),
                                        InstrumentNotes.GUITAR_NOTES.notes[1] to
                                                StringPosition(116f, 143f, 94f),
                                        InstrumentNotes.GUITAR_NOTES.notes[0] to
                                                StringPosition(116f, 223f, 104f),
                                ),
                        drawableId = R.drawable.instrument_guitar,
                )
        val UKULELE_STANDARD =
                InstrumentLayoutSpecification(
                        instrumentNotes = InstrumentNotes.UKULELE_NOTES,
                        viewportWidth = 116f,
                        viewportHeight = 208f,
                        bottomStringY = 180f,
                        knobsXOffsets =
                                mapOf(
                                        InstrumentNotes.UKULELE_NOTES.notes[2] to -40f,
                                        InstrumentNotes.UKULELE_NOTES.notes[3] to -40f,
                                        InstrumentNotes.UKULELE_NOTES.notes[1] to 40f,
                                        InstrumentNotes.UKULELE_NOTES.notes[0] to 40f,
                                ),
                        stringPositions =
                                mapOf(
                                        // Left side (low to high)
                                        InstrumentNotes.UKULELE_NOTES.notes[2] to
                                                StringPosition(28f, 61f, 48f),
                                        InstrumentNotes.UKULELE_NOTES.notes[3] to
                                                StringPosition(28f, 133f, 22f),
                                        // Right side (low to high)
                                        InstrumentNotes.UKULELE_NOTES.notes[1] to
                                                StringPosition(89f, 61f, 70f),
                                        InstrumentNotes.UKULELE_NOTES.notes[0] to
                                                StringPosition(89f, 133f, 92f),
                                ),
                        drawableId = R.drawable.instrument_ukulele,
                )
    }
}

/**
 * Represents the position of a guitar string.
 *
 * @property startX X coordinate at the top of the string
 * @property startY Y coordinate at the top of the string
 * @property bottomX X coordinate at the bottom of the string
 */
data class StringPosition(val startX: Float, val startY: Float, val bottomX: Float)
