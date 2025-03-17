package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.model.InstrumentLayoutSpecification
import org.samo_lego.katara.model.StringData
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun GuitarKnob(
        noteFreq: NoteFrequency,
        data: StringData,
        spec: InstrumentLayoutSpecification,
        isLeftSide: Boolean,
        isActive: Boolean,
        tuningDirection: TuningDirection,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scalingInfo =
                calculateCanvasScaling(
                        this.constraints.maxWidth.toFloat(),
                        this.constraints.maxHeight.toFloat(),
                        spec,
                        offsetX = data.knobOffset
                )
        val center =
                scalePosition(data.stringPosition.startX, data.stringPosition.startY, scalingInfo)

        val buttonSize = 25f * scalingInfo.scale
        val buttonSizeDp = with(density) { buttonSize.toDp() }

        // Animation values
        val scaleAnim = remember { Animatable(1f) }
        val elevationAnim = remember { Animatable(4f) }
        val rotationXAnim = remember { Animatable(0f) }
        val rotationYAnim = remember { Animatable(-15f) }

        // Remember previous state to detect changes
        var previousState by remember { mutableStateOf(Triple(isActive, tuningDirection, false)) }

        // Animation effect when state changes
        LaunchedEffect(isActive, tuningDirection) {
            val (wasActive, prevDirection, initialized) = previousState

            // Handle animation based on state changes
            when {
                // Case 1: Became active
                isActive && !wasActive -> {
                    // First ensure all animations are reset to starting position
                    resetAnimationsImmediately(
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim,
                            elevationAnim
                    )

                    animateForTuningDirection(
                            tuningDirection,
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim
                    )
                }

                // Case 2: Already active but tuning direction changed
                isActive && prevDirection != tuningDirection -> {
                    // For direction changes, stop any ongoing animations and reset
                    resetAnimationsImmediately(
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim,
                            elevationAnim
                    )

                    animateForTuningDirection(
                            tuningDirection,
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim
                    )
                }

                // Case 3: Became inactive - important to reset immediately
                !isActive && wasActive -> {
                    resetAnimationsImmediately(
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim,
                            elevationAnim
                    )
                }

                // Case 4: Inactive on first composition
                !isActive && !initialized -> {
                    // Make sure new knobs start with correct values
                    resetAnimationsImmediately(
                            rotationXAnim,
                            rotationYAnim,
                            scaleAnim,
                            elevationAnim
                    )
                }
            }

            // Update previous state
            previousState = Triple(isActive, tuningDirection, true)
        }

        // Determine knob colors based on active state and tuning direction
        val color = determineKnobColor(isActive, tuningDirection)

        // Position the button at the calculated center position
        val xOffset = with(density) { (center.x - buttonSize / 2).toDp() }
        val yOffset = with(density) { (center.y - buttonSize / 2).toDp() }

        Box(
                modifier =
                        Modifier.offset(x = xOffset, y = yOffset)
                                .size(buttonSizeDp)
                                .graphicsLayer {
                                    scaleX = scaleAnim.value
                                    scaleY = scaleAnim.value
                                    rotationX = rotationXAnim.value
                                    rotationY = rotationYAnim.value
                                    transformOrigin =
                                            TransformOrigin(if (isLeftSide) 1.1f else -0.1f, 0.5f)
                                    shadowElevation = elevationAnim.value
                                    shape = RoundedCornerShape(8.dp)
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { onClick() },
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = noteFreq.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
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

/**
 * Immediately stops any ongoing animations and resets them to default values This prevents
 * half-completed animations from sticking
 */
private suspend fun resetAnimationsImmediately(
        rotationXAnim: Animatable<Float, *>,
        rotationYAnim: Animatable<Float, *>,
        scaleAnim: Animatable<Float, *>,
        elevationAnim: Animatable<Float, *>
) {
    rotationXAnim.stop()
    rotationYAnim.stop()
    scaleAnim.stop()
    elevationAnim.stop()

    rotationXAnim.snapTo(0f)
    rotationYAnim.snapTo(-15f)
    scaleAnim.snapTo(1f)
    elevationAnim.snapTo(4f)
}

/** Animate based on tuning direction with proper cancellation support */
private suspend fun animateForTuningDirection(
        tuningDirection: TuningDirection,
        rotationXAnim: Animatable<Float, *>,
        rotationYAnim: Animatable<Float, *>,
        scaleAnim: Animatable<Float, *>
) {
    when (tuningDirection) {
        TuningDirection.TOO_LOW -> {
            try {
                rotationYAnim.animateTo(-20f, animationSpec = KnobAnimations.quickAdjustmentSpec)
                rotationXAnim.animateTo(360f, animationSpec = KnobAnimations.slowRotationSpec)
                rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
            } catch (e: CancellationException) {
                // Animation was cancelled, allow cancellation to propagate
                throw e
            }
        }
        TuningDirection.TOO_HIGH -> {
            try {
                rotationYAnim.animateTo(-10f, animationSpec = KnobAnimations.quickAdjustmentSpec)
                rotationXAnim.animateTo(-360f, animationSpec = KnobAnimations.slowRotationSpec)
                rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
            } catch (e: CancellationException) {
                // Animation was cancelled, allow cancellation to propagate
                throw e
            }
        }
        TuningDirection.IN_TUNE -> {
            try {
                scaleAnim.animateTo(0.9f, animationSpec = KnobAnimations.quickAdjustmentSpec)
                scaleAnim.animateTo(1f, animationSpec = KnobAnimations.bounceSpec)
            } catch (e: CancellationException) {
                // Animation was cancelled, allow cancellation to propagate
                throw e
            }
        }
    }
}

/** Reset all animations to default values with animations */
private suspend fun resetAnimations(
        rotationXAnim: Animatable<Float, *>,
        rotationYAnim: Animatable<Float, *>,
        scaleAnim: Animatable<Float, *>,
        elevationAnim: Animatable<Float, *>
) {
    try {
        rotationXAnim.animateTo(0f, animationSpec = KnobAnimations.quickAdjustmentSpec)
        rotationYAnim.animateTo(-15f, animationSpec = KnobAnimations.quickAdjustmentSpec)
        scaleAnim.animateTo(1f, animationSpec = KnobAnimations.quickAdjustmentSpec)
        elevationAnim.animateTo(4f, animationSpec = KnobAnimations.quickAdjustmentSpec)
    } catch (e: CancellationException) {
        // Animation was cancelled, allow cancellation to propagate
        throw e
    }
}

/** Determine knob color based on state and tuning direction */
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
