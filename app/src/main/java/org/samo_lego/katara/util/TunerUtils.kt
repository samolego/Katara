package org.samo_lego.katara.util

import org.samo_lego.katara.model.TunerPosition

object TunerUtils {
    // Tuner positions from the SVG file
    // Center coordinates of the tuner knobs (x, y)
    private val LEFT_TUNER_POSITIONS = listOf(
        TunerPosition(36f, 63f),  // D3
        TunerPosition(36f, 143f), // A2
        TunerPosition(36f, 223f)  // E2
    )

    private val RIGHT_TUNER_POSITIONS = listOf(
        TunerPosition(116f, 63f),  // G3
        TunerPosition(116f, 143f), // B3
        TunerPosition(116f, 223f)  // E4
    )

    // SVG viewport dimensions (from the vector tag)
    const val SVG_VIEWPORT_WIDTH = 153f
    const val SVG_VIEWPORT_HEIGHT = 326f

    // Size of the tuner knob from SVG (outer circle diameter)
    const val TUNER_KNOB_SIZE = 24f // Based on the SVG path (48-24=24)

    fun calculateTunerPositions(): Pair<List<TunerPosition>, List<TunerPosition>> {
        return Pair(LEFT_TUNER_POSITIONS, RIGHT_TUNER_POSITIONS)
    }
}

/**
 * Direction of tuning
 */
enum class TuningDirection {
    NONE, UP, DOWN
}
