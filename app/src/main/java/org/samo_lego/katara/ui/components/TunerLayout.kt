package org.samo_lego.katara.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.util.GuitarString
import org.samo_lego.katara.util.TunerUtils
import org.samo_lego.katara.util.TuningDirection

@Composable
fun TunerLayout(
    activeString: GuitarString?,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier,
    spec: GuitarSpecification = GuitarSpecification.STANDARD_6_STRING,
    imageSize: Float = 0.7f
) {
    val (leftTuners, rightTuners) = TunerUtils.calculateTunerPositions(spec)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(spec.viewportWidth / spec.viewportHeight)
            .scale(imageSize)
    ) {
        leftTuners.forEach { (string, position) ->
            LeftTunerWithNote(
                tuner = TunerState(
                    note = string.fullNoteName(),
                    rotation = getRotation(string, activeString, tuningDirection)
                ),
                onRotationChange = {},
                modifier = Modifier.absoluteOffset(
                    x = position.x.dp,
                    y = position.y.dp
                )
            )
        }

        rightTuners.forEach { (string, position) ->
            RightTunerWithNote(
                tuner = TunerState(
                    note = string.fullNoteName(),
                    rotation = getRotation(string, activeString, tuningDirection)
                ),
                onRotationChange = {},
                modifier = Modifier.absoluteOffset(
                    x = position.x.dp,
                    y = position.y.dp
                )
            )
        }
    }
}





private fun getRotation(
    knobString: GuitarString,
    activeString: GuitarString?,
    tuningDirection: TuningDirection
): Float {
    if (knobString != activeString) return 0f
    return when (tuningDirection) {
        TuningDirection.UP -> 45f
        TuningDirection.DOWN -> -45f
        TuningDirection.NONE -> 0f
    }
}
