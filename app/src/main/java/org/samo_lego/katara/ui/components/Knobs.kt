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
import androidx.compose.ui.unit.dp

data class TunerState(val note: String, val rotation: Float = 0f, val isActive: Boolean = false)

@Composable
fun LeftTunerWithNote(
    tuner: TunerState,
    onRotationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {  // Use Box instead of Row for precise positioning
        TunerKnob(
            rotation = tuner.rotation,
            onRotationChange = onRotationChange,
            modifier = Modifier.align(Alignment.Center)  // Center the knob in the Box
        )
        Text(
            text = tuner.note,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterEnd)  // Align text to the right of the knob
                .offset(x = (-32).dp)  // Offset text to the left of the knob
        )
    }
}

@Composable
fun RightTunerWithNote(
    tuner: TunerState,
    onRotationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {  // Use Box instead of Row for precise positioning
        TunerKnob(
            rotation = tuner.rotation,
            onRotationChange = onRotationChange,
            modifier = Modifier.align(Alignment.Center)  // Center the knob in the Box
        )
        Text(
            text = tuner.note,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)  // Align text to the left of the knob
                .offset(x = 32.dp)  // Offset text to the right of the knob
        )
    }
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
