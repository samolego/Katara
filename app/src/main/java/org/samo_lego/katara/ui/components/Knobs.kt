package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class TunerState(val note: String, val rotation: Float = 0f, val isActive: Boolean = false)

@Composable
fun GuitarTunerKnob(
    tuner: TunerState,
    isLeftSide: Boolean,
    onRotationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Center the knob
        TunerKnob(
            rotation = tuner.rotation,
            onRotationChange = onRotationChange,
            modifier = Modifier.align(Alignment.Center)
        )

        // Position text based on side
        Text(
            text = tuner.note,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(if (isLeftSide) Alignment.CenterEnd else Alignment.CenterStart)
                .offset(x = if (isLeftSide) (-32).dp else 32.dp)
        )
    }
}


@Preview
@Composable
fun LeftKnobPreview() {
    GuitarTunerKnob(
        isLeftSide = true,
        tuner = TunerState(note = "E2", isActive = true),
        onRotationChange = {},
    )
}

@Preview
@Composable
fun RighttKnobPreview() {
    GuitarTunerKnob(
        isLeftSide = false,
        tuner = TunerState(note = "E2", isActive = true),
        onRotationChange = {},
    )
}


@Composable
private fun TunerKnob(
        rotation: Float,
        onRotationChange: (Float) -> Unit,
        modifier: Modifier = Modifier
) {
    val animatedRotation = animateFloatAsState(targetValue = rotation, label = "Tuner rotation")

    Surface(
            modifier =
                    modifier.size(24.dp).rotate(animatedRotation.value).pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onRotationChange(rotation + dragAmount.y)
                        }
                    },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer
    ) {}
}
