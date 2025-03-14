package org.samo_lego.katara.model

import org.samo_lego.katara.R
import org.samo_lego.katara.util.NoteFrequency

/** Represents specifications for a guitar type, including string and tuner positions. */
data class InstrumentLayoutSpecification(
        val viewportWidth: Float,
        val viewportHeight: Float,
        val bottomStringY: Float,
        val stringDataMap: Map<NoteFrequency, StringData>,
        val drawableId: Int,

        ) {
    companion object {
            lateinit var GUITAR_STANDARD: InstrumentLayoutSpecification
            lateinit var UKULELE_STANDARD: InstrumentLayoutSpecification
            init {
                    {
                        // Guitar
                        val knobOffset = 50f
                        val leftStartX = 36f
                        val rightStartX = 116f

                            val topY = 63f
                            val middleY = 143f
                            val bottomY = 223f

                            val stringDataMap = mapOf(
                                    NoteFrequency.D3 to StringData(4, -knobOffset, StringPosition(leftStartX, topY, 69f)),
                                    NoteFrequency.A2 to StringData(5, -knobOffset, StringPosition(leftStartX, middleY, 57f)),
                                    NoteFrequency.E2 to StringData(6, -knobOffset, StringPosition(leftStartX, bottomY, 47f)),
                                    NoteFrequency.G3 to StringData(3, knobOffset, StringPosition(rightStartX, topY, 83f)),
                                    NoteFrequency.B3 to StringData(2, knobOffset, StringPosition(rightStartX, middleY, 94f)),
                                    NoteFrequency.E4 to StringData(1, knobOffset, StringPosition(rightStartX, bottomY, 104f)),
                            )
                            GUITAR_STANDARD = InstrumentLayoutSpecification(
                                    viewportWidth = 153f,
                                    viewportHeight = 326f,
                                    bottomStringY = 297f,
                                    stringDataMap = stringDataMap,
                                    drawableId = R.drawable.instrument_guitar,
                            )
                    }

                    {
                            // Ukulele
                            val knobOffset = 40f
                            val leftStartX = 28f
                            val rightStartX = 89f

                            val topY = 61f
                            val bottomY = 133f

                            val stringDataMap = mapOf(
                                    NoteFrequency.C4 to StringData(3, -knobOffset, StringPosition(leftStartX, topY, 48f)),
                                    NoteFrequency.G4 to StringData(4, -knobOffset, StringPosition(leftStartX, bottomY, 22f)),
                                    NoteFrequency.E4 to StringData(2, knobOffset, StringPosition(rightStartX, topY, 70f)),
                                    NoteFrequency.A4 to StringData(1, knobOffset, StringPosition(rightStartX, bottomY, 92f)),
                            )

                            UKULELE_STANDARD = InstrumentLayoutSpecification(
                                    viewportWidth = 116f,
                                    viewportHeight = 208f,
                                    bottomStringY = 180f,
                                    stringDataMap = stringDataMap,
                                    drawableId = R.drawable.instrument_ukulele,
                            )
                    }
            }
    }
}

/**
 * Represents the data for the instrument string.
 * Negative knob offsets are the ones to be drawn on the left side,
 * as note name and knob position will swap places.
 */
data class StringData(val index: Int, val knobOffset: Float, val stringPosition: StringPosition)

/**
 * Represents the position of a guitar string.
 *
 * @property startX X coordinate at the top of the string
 * @property startY Y coordinate at the top of the string
 * @property bottomX X coordinate at the bottom of the string
 */
data class StringPosition(val startX: Float, val startY: Float, val bottomX: Float)