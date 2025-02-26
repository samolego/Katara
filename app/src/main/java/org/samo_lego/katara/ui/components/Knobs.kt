package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.util.TuningDirection

data class TunerState(
        val note: String,
        val rotation: Float = 0f,
        val isActive: Boolean = false,
        val tuningDirection: TuningDirection = TuningDirection.IN_TUNE
)

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
                isActive = tuner.isActive,
                tuningDirection = tuner.tuningDirection,
                onRotationChange = onRotationChange,
                modifier = Modifier.align(Alignment.Center)
        )

        // Position text based on side
        Text(
                text = tuner.note,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier =
                        Modifier.align(
                                        if (isLeftSide) Alignment.CenterEnd
                                        else Alignment.CenterStart
                                )
                                .offset(x = if (isLeftSide) (-32).dp else 32.dp)
        )
    }
}

@Composable
private fun TunerKnob(
        rotation: Float,
        isActive: Boolean,
        tuningDirection: TuningDirection,
        onRotationChange: (Float) -> Unit,
        modifier: Modifier = Modifier
) {
    // Create an infinite transition for continuous rotation
    val infiniteTransition = rememberInfiniteTransition(label = "Knob rotation transition")

    // Animate based on tuning direction
    val animatedRotation by
            when (tuningDirection) {
                TuningDirection.TOO_LOW -> {
                    // Rotate from bottom to top (180 to 0 degrees) if active
                    if (isActive) {
                        infiniteTransition.animateFloat(
                                initialValue = 180f,
                                targetValue = 0f,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(800, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                        ),
                                label = "Knob rotation too low"
                        )
                    } else {
                        infiniteTransition.animateFloat(
                                initialValue = rotation,
                                targetValue = rotation,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(1000),
                                                repeatMode = RepeatMode.Restart
                                        ),
                                label = "Static rotation"
                        )
                    }
                }
                TuningDirection.TOO_HIGH -> {
                    // Rotate from top to bottom (0 to 180 degrees) if active
                    if (isActive) {
                        infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 180f,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(800, easing = LinearEasing),
                                                repeatMode = RepeatMode.Restart
                                        ),
                                label = "Knob rotation too high"
                        )
                    } else {
                        infiniteTransition.animateFloat(
                                initialValue = rotation,
                                targetValue = rotation,
                                animationSpec =
                                        infiniteRepeatable(
                                                animation = tween(1000),
                                                repeatMode = RepeatMode.Restart
                                        ),
                                label = "Static rotation"
                        )
                    }
                }
                TuningDirection.IN_TUNE -> {
                    // No rotation animation when in tune
                    infiniteTransition.animateFloat(
                            initialValue = rotation,
                            targetValue = rotation,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Restart
                                    ),
                            label = "Static rotation"
                    )
                }
            }

    Surface(
            modifier =
                    modifier.size(24.dp).rotate(animatedRotation).pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Manual rotation still available if needed
                            onRotationChange(rotation + dragAmount.y)
                        }
                    },
            shape = MaterialTheme.shapes.small,
            color =
                    when (tuningDirection) {
                        TuningDirection.IN_TUNE -> MaterialTheme.colorScheme.secondaryContainer
                        TuningDirection.TOO_LOW -> MaterialTheme.colorScheme.errorContainer
                        TuningDirection.TOO_HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                    }
    ) {}
}

@Preview
@Composable
fun TunerKnobsPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            GuitarTunerKnob(
                    isLeftSide = true,
                    tuner =
                            TunerState(
                                    note = "E2",
                                    isActive = true,
                                    tuningDirection = TuningDirection.TOO_LOW
                            ),
                    onRotationChange = {},
            )

            GuitarTunerKnob(
                    isLeftSide = false,
                    tuner =
                            TunerState(
                                    note = "A2",
                                    isActive = true,
                                    tuningDirection = TuningDirection.TOO_HIGH
                            ),
                    onRotationChange = {},
            )
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            GuitarTunerKnob(
                    isLeftSide = true,
                    tuner =
                            TunerState(
                                    note = "D3",
                                    isActive = true,
                                    tuningDirection = TuningDirection.IN_TUNE
                            ),
                    onRotationChange = {},
            )

            GuitarTunerKnob(
                    isLeftSide = false,
                    tuner = TunerState(note = "G3", isActive = false),
                    onRotationChange = {},
            )
        }
    }
}
