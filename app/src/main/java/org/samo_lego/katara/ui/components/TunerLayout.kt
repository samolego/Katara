package org.samo_lego.katara.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.util.GuitarString
import org.samo_lego.katara.util.TuningDirection

@Composable
fun TunerLayout(
    activeString: GuitarString?,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side tuners
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 16.dp)
        ) {
            LeftTunerWithNote(
                tuner = TunerState("D3", getRotation(GuitarString.D3, activeString, tuningDirection)),
                onRotationChange = {}
            )
            LeftTunerWithNote(
                tuner = TunerState("A2", getRotation(GuitarString.A2, activeString, tuningDirection)),
                onRotationChange = {}
            )
            LeftTunerWithNote(
                tuner = TunerState("E2", getRotation(GuitarString.E2, activeString, tuningDirection)),
                onRotationChange = {}
            )
        }

        // Right side tuners
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            RightTunerWithNote(
                tuner = TunerState("G3", getRotation(GuitarString.G3, activeString, tuningDirection)),
                onRotationChange = {}
            )
            RightTunerWithNote(
                tuner = TunerState("B3", getRotation(GuitarString.B3, activeString, tuningDirection)),
                onRotationChange = {}
            )
            RightTunerWithNote(
                tuner = TunerState("E4", getRotation(GuitarString.E4, activeString, tuningDirection)),
                onRotationChange = {}
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
