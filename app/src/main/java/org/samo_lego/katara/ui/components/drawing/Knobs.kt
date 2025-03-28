package org.samo_lego.katara.ui.components.drawing

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.samo_lego.katara.instrument.InstrumentLayoutSpecification
import org.samo_lego.katara.instrument.StringData
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.tuner.TuningDirection
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
import org.samo_lego.katara.ui.util.calculateCanvasScaling
import org.samo_lego.katara.ui.util.scalePosition

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

        // Animation values with initial values matching inactive state
        val scaleAnim = remember { Animatable(1f) }
        val elevationAnim = remember { Animatable(4f) }
        val rotationXAnim = remember { Animatable(0f) }
        val rotationYAnim = remember { Animatable(getInitialYRotation(isLeftSide)) }

        // Enhanced animation coordinator for better state transitions
        val animationCoordinator = remember { KnobAnimationCoordinator() }

        // Animation effect responding to state changes
        LaunchedEffect(isActive, tuningDirection) {
            // Update the animation state
            animationCoordinator.updateState(isActive, tuningDirection)

            // Use coroutineScope to manage child animation jobs
            coroutineScope {
                // Cancel any active jobs before starting new ones
                animationCoordinator.activeJob?.cancelAndJoin()

                // Start new animation sequence based on state
                val newJob = launch {
                    if (isActive) {
                        when (tuningDirection) {
                            TuningDirection.TOO_LOW -> {
                                // Launch continuous rotation for too low state
                                launch {
                                    // Ensure knob is in proper starting position
                                    rotationYAnim.snapTo(if (isLeftSide) -20f else 20f)

                                    // Continuously rotate until interrupted
                                    while (true) {
                                        rotationXAnim.animateTo(
                                                rotationXAnim.value + 360f,
                                                animationSpec =
                                                        KnobAnimations.responsiveRotationSpec
                                        )
                                    }
                                }
                            }
                            TuningDirection.TOO_HIGH -> {
                                // Launch continuous rotation for too high state
                                launch {
                                    // Ensure knob is in proper starting position
                                    rotationYAnim.snapTo(if (isLeftSide) -10f else 10f)

                                    // Continuously rotate until interrupted
                                    while (true) {
                                        rotationXAnim.animateTo(
                                                rotationXAnim.value - 360f,
                                                animationSpec =
                                                        KnobAnimations.responsiveRotationSpec
                                        )
                                    }
                                }
                            }
                            TuningDirection.IN_TUNE -> {
                                // If we've been rotating, perform a smooth return to neutral
                                if (rotationYAnim.value != getInitialYRotation(isLeftSide) ||
                                                rotationXAnim.value % 360f != 0f
                                ) {

                                    // Complete the current rotation in the same direction to avoid
                                    // sudden reversal

                                    // Calculate target rotation to nearest 360 multiple in the same
                                    // direction
                                    val targetRotation = (rotationXAnim.value / 360f).toInt() * 360f + 360f
                                    Log.d("direction", "Target rotation: $targetRotation, tuning direction: $tuningDirection")

                                    // Continue rotation in same direction to target
                                    rotationXAnim.animateTo(
                                            targetRotation,
                                            animationSpec =
                                                    KnobAnimations.finishRotationSmoothlySpec
                                    )

                                    // Return to neutral Y rotation
                                    rotationYAnim.animateTo(
                                            getInitialYRotation(isLeftSide),
                                            animationSpec = KnobAnimations.finishSmoothlySpec
                                    )
                                }

                                // Perform "in tune" pulse animation
                                scaleAnim.animateTo(
                                        0.9f,
                                        animationSpec = KnobAnimations.instantSpec
                                )
                                scaleAnim.animateTo(1.1f, animationSpec = KnobAnimations.bounceSpec)
                                scaleAnim.animateTo(1f, animationSpec = KnobAnimations.bounceSpec)
                            }
                        }
                    } else {
                        // When becoming inactive, ensure a smooth return to default state
                        rotationYAnim.animateTo(
                                getInitialYRotation(isLeftSide),
                                animationSpec = KnobAnimations.finishSmoothlySpec
                        )

                        // Calculate target rotation to nearest 360 multiple in the same direction
                        val targetRotation = (rotationXAnim.value / 360f).toInt() * 360f + 360f
                        Log.d("direction", "Target rotation: $targetRotation, tuning direwction: $tuningDirection")

                        // Continue rotation in same direction to target
                        rotationXAnim.animateTo(
                                targetRotation,
                                animationSpec = KnobAnimations.finishRotationSmoothlySpec
                        )

                        scaleAnim.animateTo(1f, animationSpec = KnobAnimations.finishSmoothlySpec)
                        elevationAnim.animateTo(
                                4f,
                                animationSpec = KnobAnimations.finishSmoothlySpec
                        )
                    }
                }

                // Store the job for potential cancellation
                animationCoordinator.activeJob = newJob
            }
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
                                    // shadowElevation = elevationAnim.value
                                    shape = RoundedCornerShape(16.dp)
                                }
                                .clip(RoundedCornerShape(16.dp))
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

private fun getInitialYRotation(isLeftSide: Boolean): Float {
    return if (isLeftSide) 15f else -15f
}

/** Enhanced coordinator that manages animation states and transitions */
private class KnobAnimationCoordinator {
    var activeDirection: TuningDirection = TuningDirection.IN_TUNE
    var isActive: Boolean = false
    var activeJob: Job? = null

    fun updateState(active: Boolean, direction: TuningDirection) {
        isActive = active
        activeDirection = direction
    }
}

/** Animation specifications for tuner knob animations */
private object KnobAnimations {
    // Instant start for immediate visual feedback
    val instantSpec: AnimationSpec<Float> = tween(durationMillis = 50, easing = LinearEasing)

    // Responsive rotation with fast start, slower finish
    val responsiveRotationSpec: AnimationSpec<Float> =
            tween(durationMillis = 1500, easing = FastOutSlowInEasing)

    // Quick but natural-feeling adjustments
    val quickAdjustmentSpec: AnimationSpec<Float> =
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 200f)

    // Bouncy effect for in-tune feedback
    val bounceSpec: AnimationSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 180f)

    // For smooth completion of animations when becoming inactive
    val finishSmoothlySpec: AnimationSpec<Float> =
            tween(durationMillis = 300, easing = FastOutSlowInEasing)

    // For completing rotations smoothly
    val finishRotationSmoothlySpec: AnimationSpec<Float> =
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
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
