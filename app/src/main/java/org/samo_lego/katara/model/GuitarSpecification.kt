package org.samo_lego.katara.model

import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.InstrumentType

/** Represents specifications for a guitar type, including string and tuner positions. */
data class GuitarSpecification(
        val viewportWidth: Float,
        val viewportHeight: Float,
        val stringPositions: Map<InstrumentString, StringPosition>,
        val bottomStringY: Float,
        val knobsXOffsets: Map<InstrumentString, Float>,
) {
    companion object {
        val STANDARD_6_STRING =
                GuitarSpecification(
                        viewportWidth = 153f,
                        viewportHeight = 326f,
                        bottomStringY = 297f,
                        knobsXOffsets =
                                mapOf(
                                        InstrumentType.GUITAR_STANDARD.strings[3] to -50f,
                                        InstrumentType.GUITAR_STANDARD.strings[4] to -50f,
                                        InstrumentType.GUITAR_STANDARD.strings[5] to -50f,
                                        InstrumentType.GUITAR_STANDARD.strings[2] to 50f,
                                        InstrumentType.GUITAR_STANDARD.strings[1] to 50f,
                                        InstrumentType.GUITAR_STANDARD.strings[0] to 50f,
                                ),
                        stringPositions =
                                mapOf(
                                        // Left side (low to high)
                                        InstrumentType.GUITAR_STANDARD.strings[3] to
                                                StringPosition(36f, 63f, 69f),
                                        InstrumentType.GUITAR_STANDARD.strings[4] to
                                                StringPosition(36f, 143f, 57f),
                                        InstrumentType.GUITAR_STANDARD.strings[5] to
                                                StringPosition(36f, 223f, 47f),
                                        // Right side (low to high)
                                        InstrumentType.GUITAR_STANDARD.strings[2] to
                                                StringPosition(116f, 63f, 84f),
                                        InstrumentType.GUITAR_STANDARD.strings[1] to
                                                StringPosition(116f, 143f, 94f),
                                        InstrumentType.GUITAR_STANDARD.strings[0] to
                                                StringPosition(116f, 223f, 104f),
                                )
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
