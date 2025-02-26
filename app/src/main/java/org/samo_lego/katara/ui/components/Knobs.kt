package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
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
                isLeftSide = isLeftSide,
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
                                .offset(x = if (isLeftSide) (-48).dp else 48.dp)
        )
    }
}

@Composable
private fun TunerKnob(
        rotation: Float,
        isActive: Boolean,
        isLeftSide: Boolean,
        tuningDirection: TuningDirection,
        onRotationChange: (Float) -> Unit,
        modifier: Modifier = Modifier
) {
    // Animation values
    val scaleXYAnim = remember { Animatable(1f) }
    val elevationAnim = remember { Animatable(4f) }
    val rotationXAnim = remember { Animatable(0f) }

    // Use consistent animation specs to ensure uniform animation speeds
    val slowRotationSpec: AnimationSpec<Float> = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
    val quickAdjustmentSpec: AnimationSpec<Float> = spring(stiffness = 150f)
    val bounceSpec: AnimationSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 150f
    )

    // Track the previous tuning direction to detect changes
    val previousTuningDirection = remember { mutableStateOf(tuningDirection) }
    val previousActive = remember { mutableStateOf(isActive) }

    // When activation state changes, trigger animation
    LaunchedEffect(isActive, tuningDirection) {
        // First handle the case when a knob becomes active
        if (isActive && !previousActive.value) {
            when (tuningDirection) {
                TuningDirection.TOO_LOW -> {
                    // Reset rotation first if needed
                    if (rotationXAnim.value != 0f) {
                        rotationXAnim.snapTo(0f)
                    }
                    rotationYAnim.animateTo(-20f, animationSpec = quickAdjustmentSpec)
                    rotationXAnim.animateTo(targetValue = 360f, animationSpec = slowRotationSpec)
                    rotationYAnim.animateTo(-15f, animationSpec = quickAdjustmentSpec)
                }
                TuningDirection.TOO_HIGH -> {
                    // Reset rotation first if needed
                    if (rotationXAnim.value != 0f) {
                        rotationXAnim.snapTo(0f)
                    }
                    rotationYAnim.animateTo(-10f, animationSpec = quickAdjustmentSpec)
                    rotationXAnim.animateTo(targetValue = -360f, animationSpec = slowRotationSpec)
                    rotationYAnim.animateTo(-15f, animationSpec = quickAdjustmentSpec)
                }
                TuningDirection.IN_TUNE -> {
                    scaleXYAnim.animateTo(targetValue = 0.9f, animationSpec = quickAdjustmentSpec)
                    scaleXYAnim.animateTo(targetValue = 1f, animationSpec = bounceSpec)
                    scaleXYAnim.animateTo(1f, animationSpec = bounceSpec)
                }
            }
        }
        // Handle the case when tuning direction changes while already active
        else if (isActive && previousActive.value && tuningDirection != previousTuningDirection.value) {
            // Reset X rotation to start fresh
            rotationXAnim.snapTo(0f)

            when (tuningDirection) {
                TuningDirection.TOO_LOW -> {
                    rotationYAnim.animateTo(-20f, animationSpec = quickAdjustmentSpec)
                    rotationXAnim.animateTo(targetValue = 360f, animationSpec = slowRotationSpec)
                    rotationYAnim.animateTo(-15f, animationSpec = quickAdjustmentSpec)
                }
                TuningDirection.TOO_HIGH -> {
                    rotationYAnim.animateTo(-10f, animationSpec = quickAdjustmentSpec)
                    rotationXAnim.animateTo(targetValue = -360f, animationSpec = slowRotationSpec)
                    rotationYAnim.animateTo(-15f, animationSpec = quickAdjustmentSpec)
                }
                TuningDirection.IN_TUNE -> {
                    scaleXYAnim.animateTo(targetValue = 0.9f, animationSpec = quickAdjustmentSpec)
                    scaleXYAnim.animateTo(targetValue = 1f, animationSpec = bounceSpec)
                    scaleXYAnim.animateTo(1f, animationSpec = bounceSpec)
                    rotationXAnim.animateTo(targetValue = 0f, animationSpec = quickAdjustmentSpec)
                }
            }
        }
        // Handle inactive state
        else if (!isActive && previousActive.value) {
            rotationXAnim.animateTo(targetValue = 0f, animationSpec = quickAdjustmentSpec)
            scaleXYAnim.animateTo(targetValue = 1f, animationSpec = quickAdjustmentSpec)
            elevationAnim.animateTo(4f, animationSpec = quickAdjustmentSpec)
        }

        // Update previous states
        previousActive.value = isActive
        previousTuningDirection.value = tuningDirection
    }

    // Rest of your knob rendering code remains the same
    // Determine knob colors based on active state and tuning direction
    val color =
            if (isActive) {
                when (tuningDirection) {
                    TuningDirection.IN_TUNE -> TuneOk
                    TuningDirection.TOO_LOW -> TuneTooLow
                    TuningDirection.TOO_HIGH -> TuneTooHigh
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

    val topColor = color.lighten(0.1f)
    val middleColor = color
    val bottomColor = color.darken(0.1f)

    // Add light and shadow colors to enhance 3D effect
    val lightEdgeColor = color.lighten(0.2f)
    val shadowEdgeColor = color.darken(0.2f)

    Box(modifier = Modifier.padding(4.dp).graphicsLayer(clip = false)) {
        Surface(
                modifier =
                        modifier.size(32.dp)
                                .shadow(elevationAnim.value.dp)
                                .graphicsLayer {
                                    scaleX = scaleXYAnim.value
                                    rotationX = rotationXAnim.value
                                    rotationY = rotationYAnim.value
                                    transformOrigin = TransformOrigin(if (isLeftSide) 1.1f else -0.1f, 0.5f)
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        onRotationChange(rotation + dragAmount.y)
                                    }
                                },
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw a stronger gradient from top to bottom for clear directionality
                drawRect(
                        brush = Brush.verticalGradient(
                                colors = listOf(topColor, middleColor, bottomColor),
                                startY = 0f,
                                endY = size.height
                        )
                )

                // Add a subtle center line
                drawLine(
                    color = color.lighten(0.1f),
                    start = Offset(size.width * 0.2f, size.height * 0.5f),
                    end = Offset(size.width * 0.8f, size.height * 0.5f),
                    strokeWidth = size.height * 0.06f
                )

                // Right edge shadow (appears further)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, shadowEdgeColor),
                        startY = size.width * 0.6f,
                        endY = size.width
                    )
                )
            }
        }
    }
}

// Helper extension functions for color manipulation
private fun Color.lighten(amount: Float): Color {
    return copy(
            red = (red + amount).coerceAtMost(1f),
            green = (green + amount).coerceAtMost(1f),
            blue = (blue + amount).coerceAtMost(1f)
    )
}

private fun Color.darken(amount: Float): Color {
    return copy(
            red = (red - amount).coerceAtLeast(0f),
            green = (green - amount).coerceAtLeast(0f),
            blue = (blue - amount).coerceAtLeast(0f)
    )
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
