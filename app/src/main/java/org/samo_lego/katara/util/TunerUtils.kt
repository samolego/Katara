package org.samo_lego.katara.util

import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.model.TunerPosition

object TunerUtils {
    fun calculateTunerPositions(spec: GuitarSpecification = GuitarSpecification.STANDARD_6_STRING):
            Pair<List<Pair<GuitarString, TunerPosition>>, List<Pair<GuitarString, TunerPosition>>> {

        val leftStrings = listOf(GuitarString.D3, GuitarString.A2, GuitarString.E2)
        val rightStrings = listOf(GuitarString.G3, GuitarString.B3, GuitarString.E4)

        val leftTuners = leftStrings.map { string ->
            val pos = spec.stringPositions[string]!!
            string to TunerPosition(pos.startX, pos.startY)
        }

        val rightTuners = rightStrings.map { string ->
            val pos = spec.stringPositions[string]!!
            string to TunerPosition(pos.startX, pos.startY)
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
