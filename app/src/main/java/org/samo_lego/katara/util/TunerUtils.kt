package org.samo_lego.katara.util

import org.samo_lego.katara.model.TunerPosition

object TunerUtils {

    // Tuner positions from the SVG
    private val TUNER_Y_POSITIONS = listOf(175f, 260f, 340f)
    private const val LEFT_TUNER_X = 40f
    private const val RIGHT_TUNER_X = -40f

    fun calculateTunerPositions(): Pair<List<TunerPosition>, List<TunerPosition>> {
        val leftTuners = TUNER_Y_POSITIONS.map { y ->
            TunerPosition(LEFT_TUNER_X, y)
        }

        val rightTuners = TUNER_Y_POSITIONS.map { y ->
            TunerPosition(RIGHT_TUNER_X, y)
        }

        return Pair(leftTuners, rightTuners)
    }
}

/**
 * Direction of tuning
 */
enum class TuningDirection {
    NONE, UP, DOWN
}
