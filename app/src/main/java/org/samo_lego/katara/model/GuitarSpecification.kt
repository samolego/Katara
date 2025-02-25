package org.samo_lego.katara.model

import androidx.compose.ui.geometry.Offset
import org.samo_lego.katara.util.GuitarString

/**
 * Represents specifications for a guitar type, including string and tuner positions.
 */
data class GuitarSpecification(
    val viewportWidth: Float,
    val viewportHeight: Float,
    val stringPositions: Map<GuitarString, StringPosition>,
    val bottomStringY: Float
) {
    companion object {
        val STANDARD_6_STRING = GuitarSpecification(
            viewportWidth = 153f,
            viewportHeight = 326f,
            bottomStringY = 297f,
            stringPositions = mapOf(
                // Left side (low to high)
                GuitarString.D3 to StringPosition(36f, 63f, 69f),
                GuitarString.A2 to StringPosition(36f, 143f, 57f),
                GuitarString.E2 to StringPosition(36f, 223f, 47f),
                // Right side (low to high)
                GuitarString.G3 to StringPosition(116f, 63f, 80f),
                GuitarString.B3 to StringPosition(116f, 143f, 90f),
                GuitarString.E4 to StringPosition(116f, 223f, 101f)
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
data class StringPosition(
    val startX: Float,
    val startY: Float,
    val bottomX: Float
)

/**
 * Extension function to get tuner position for a string
 */
fun StringPosition.toTunerPosition() = TunerPosition(startX, startY)
