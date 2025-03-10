package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Animation specifications for tuner knob animations
 */
private object KnobAnimations {
    // Consistent animation specs for different animation types
    val slowRotationSpec: AnimationSpec<Float> = tween(
        durationMillis = 2000,
        easing = FastOutSlowInEasing
    )

    val quickAdjustmentSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 150f
    )

    val bounceSpec: AnimationSpec<Float> = spring(
        dampingRatio = 0.7f,
        stiffness = 150f
    )
}

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
            modifier = Modifier
                .align(if (isLeftSide) Alignment.CenterEnd else Alignment.CenterStart)
                .offset(
                    x = if (isLeftSide) (-48).dp else 48.dp,
                    y = 0.dp
                )
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
    val scaleAnim = remember { Animatable(1f) }
    val elevationAnim = remember { Animatable(4f) }
    val rotationXAnim = remember { Animatable(0f) }
    val rotationYAnim = remember { Animatable(-15f) }

    // Remember previous state to detect changes
    var previousState by remember {
        mutableStateOf(Triple(isActive, tuningDirection, false))
    }

    // Use a single LaunchedEffect to handle all animation logic
    LaunchedEffect(isActive, tuningDirection) {
        val (wasActive, prevDirection, _) = previousState
        val stateChanged = wasActive != isActive || prevDirection != tuningDirection

        if (!stateChanged) return@LaunchedEffect

        // Handle animation based on state changes
        when {
            // Case 1: Became active
            isActive && !wasActive -> {
                animateForTuningDirection(
                    tuningDirection,
                    rotationXAnim,
                    rotationYAnim,
                    scaleAnim
                )
            }

            // Case 2: Already active but tuning direction changed
            isActive -> {
                // Reset X rotation for a clean start
                rotationXAnim.snapTo(0f)
                animateForTuningDirection(
                    tuningDirection,
                    rotationXAnim,
                    rotationYAnim,
                    scaleAnim
                )
            }

            // Case 3: Became inactive
            !isActive && wasActive -> {
                resetAnimations(rotationXAnim, rotationYAnim, scaleAnim, elevationAnim)
            }
        }

        // Update previous state
        previousState = Triple(isActive, tuningDirection, true)
    }

    // Determine knob colors based on active state and tuning direction
    val color = determineKnobColor(isActive, tuningDirection)

    val topColor = color.lighten(0.1f)
    val middleColor = color
    val bottomColor = color.darken(0.1f)
    val shadowEdgeColor = color.darken(0.2f)

    Box(modifier = Modifier.padding(4.dp).graphicsLayer(clip = false)) {
        Surface(
            modifier =
                modifier.size(32.dp)
                    .shadow(elevationAnim.value.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        rotationX = rotationXAnim.value
                        rotationY = rotationYAnim.value
                        transformOrigin = TransformOrigin(
                            if (isLeftSide) 1.1f else -0.1f,
                            0.5f
                        )
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
                // Draw gradient background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(topColor, middleColor, bottomColor),
                        startY = 0f,
                        endY = size.height
                    )
                )

                // Center indicator line
                drawLine(
                    color = color.lighten(0.1f),
                    start = Offset(size.width * 0.2f, size.height * 0.5f),
                    end = Offset(size.width * 0.8f, size.height * 0.5f),
                    strokeWidth = size.height * 0.06f
                )

                // Edge shadow for depth
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

/**
 * Animate based on tuning direction
 */
private suspend fun animateForTuningDirection(
    tuningDirection: TuningDirection,
    rotationXAnim: Animatable<Float, *>,
    rotationYAnim: Animatable<Float, *>,
    scaleAnim: Animatable<Float, *>
) {
    when (tuningDirection) {
        TuningDirection.TOO_LOW -> {
            rotationYAnim.animateTo(-20f, animationSpec = KnobAnimations.quickAdjustmentSpec)
            rotationXAnim.animateTo(360f, animationSpec = KnobAnimations.slowRotationSpec)
            rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
        }
        TuningDirection.TOO_HIGH -> {
            rotationYAnim.animateTo(-10f, animationSpec = KnobAnimations.quickAdjustmentSpec)
            rotationXAnim.animateTo(-360f, animationSpec = KnobAnimations.slowRotationSpec)
            rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
        }
        TuningDirection.IN_TUNE -> {
            scaleAnim.animateTo(0.9f, animationSpec = KnobAnimations.quickAdjustmentSpec)
            scaleAnim.animateTo(1f, animationSpec = KnobAnimations.bounceSpec)
        }
    }
}

/**
 * Reset all animations to default values
 */
private suspend fun resetAnimations(
    rotationXAnim: Animatable<Float, *>,
    rotationYAnim: Animatable<Float, *>,
    scaleAnim: Animatable<Float, *>,
    elevationAnim: Animatable<Float, *>
) {
    rotationXAnim.animateTo(0f, animationSpec = KnobAnimations.quickAdjustmentSpec)
    rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
    scaleAnim.animateTo(1f, animationSpec = KnobAnimations.quickAdjustmentSpec)
    elevationAnim.animateTo(4f, animationSpec = KnobAnimations.quickAdjustmentSpec)
}

/**
 * Determine knob color based on state and tuning direction
 */
@Composable
private fun determineKnobColor(isActive: Boolean, tuningDirection: TuningDirection): Color {
    return if (isActive) {
        when (tuningDirection) {
            TuningDirection.IN_TUNE -> TuneOk
            TuningDirection.TOO_LOW -> TuneTooLow
            TuningDirection.TOO_HIGH -> TuneTooHigh
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
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
