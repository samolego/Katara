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
import androidx.compose.ui.graphics.graphicsLayer
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
    // Create an infinite transition for continuous flipping
    val infiniteTransition = rememberInfiniteTransition(label = "Knob flipping transition")

    // Animate scaleY for flipping effect
    val animatedScaleY by
            when {
                isActive && tuningDirection == TuningDirection.TOO_LOW -> {
                    // Flip from bottom to top when too low
                    infiniteTransition.animateFloat(
                            initialValue = -1f,
                            targetValue = 1f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "Knob flipping too low"
                    )
                }
                isActive && tuningDirection == TuningDirection.TOO_HIGH -> {
                    // Flip from top to bottom when too high
                    infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = -1f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "Knob flipping too high"
                    )
                }
                else -> {
                    // No flipping when in tune or inactive
                    infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "Static scale"
                    )
                }
            }

    // Add a slight rotation to enhance 3D effect during flipping
    val enhancementRotation by
            when {
                isActive &&
                        (tuningDirection == TuningDirection.TOO_LOW ||
                                tuningDirection == TuningDirection.TOO_HIGH) -> {
                    infiniteTransition.animateFloat(
                            initialValue = -5f,
                            targetValue = 5f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "Enhancement rotation"
                    )
                }
                else -> {
                    infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 0f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "Static rotation"
                    )
                }
            }

    // Determine knob color based on active state and tuning direction
    val knobColor =
            if (isActive) {
                when (tuningDirection) {
                    TuningDirection.IN_TUNE -> MaterialTheme.colorScheme.secondaryContainer
                    TuningDirection.TOO_LOW -> MaterialTheme.colorScheme.errorContainer
                    TuningDirection.TOO_HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

    Surface(
            modifier =
                    modifier.size(24.dp)
                            .graphicsLayer {
                                scaleY = animatedScaleY
                                rotationX = enhancementRotation
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Manual rotation still available if needed
                                    onRotationChange(rotation + dragAmount.y)
                                }
                            },
            shape = MaterialTheme.shapes.small,
            color = knobColor
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
