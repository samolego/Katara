package org.samo_lego.katara.ui.components

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.samo_lego.katara.model.InstrumentLayoutSpecification
import org.samo_lego.katara.model.StringData
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.tuner.TuningDirection

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

        // Track animation completion status to avoid re-triggering
        val animationInProgress = remember { mutableStateOf(false) }

        // Animation coordinator - single source of truth for animation state
        val animationCoordinator = remember { KnobAnimationCoordinator() }

        // Animation effect responding to state changes
        LaunchedEffect(isActive, tuningDirection) {
            // Don't start another animation if one's already in progress
            // unless it's a critical state change
            if (animationInProgress.value && isActive) {
                if (animationCoordinator.activeDirection != tuningDirection) {
                    // Direction changed while animating - transition smoothly
                    animationCoordinator.updateDirection(tuningDirection)
                }
                // Otherwise continue current animation
                return@LaunchedEffect
            }

            // Handle activation/deactivation
            if (isActive) {
                // Immediate response - start animation right away
                animationInProgress.value = true

                // Set the active direction in coordinator
                animationCoordinator.updateDirection(tuningDirection)

                // Start animation sequence immediately
                launch {
                    try {
                        when (tuningDirection) {
                            TuningDirection.TOO_LOW -> {
                                // Quick prepare animation
                                rotationYAnim.animateTo(
                                        if (isLeftSide) -20f else 20f,
                                        animationSpec = KnobAnimations.instantSpec
                                )

                                // Main rotation with faster start
                                rotationXAnim.animateTo(
                                        360f,
                                        animationSpec = KnobAnimations.responsiveRotationSpec
                                )

                                // Return to neutral position
                                rotationYAnim.animateTo(
                                        getInitialYRotation(isLeftSide),
                                        animationSpec = KnobAnimations.quickAdjustmentSpec
                                )
                            }
                            TuningDirection.TOO_HIGH -> {
                                // Quick prepare animation
                                rotationYAnim.animateTo(
                                        if (isLeftSide) -10f else 10f,
                                        animationSpec = KnobAnimations.instantSpec
                                )

                                // Main rotation with faster start
                                rotationXAnim.animateTo(
                                        -360f,
                                        animationSpec = KnobAnimations.responsiveRotationSpec
                                )

                                // Return to neutral position
                                rotationYAnim.animateTo(
                                        getInitialYRotation(isLeftSide),
                                        animationSpec = KnobAnimations.quickAdjustmentSpec
                                )
                            }
                            TuningDirection.IN_TUNE -> {
                                // Faster pulse animation
                                scaleAnim.animateTo(
                                        0.9f,
                                        animationSpec = KnobAnimations.instantSpec
                                )
                                scaleAnim.animateTo(1.1f, animationSpec = KnobAnimations.bounceSpec)
                                scaleAnim.animateTo(1f, animationSpec = KnobAnimations.bounceSpec)
                            }
                        }
                    } finally {
                        // Mark animation as complete whether it finished or was canceled
                        animationInProgress.value = false
                    }
                }
            } else {
                // When becoming inactive, ensure a smooth finish rather than abrupt reset
                // Only animate if we need to return to default state
                if (rotationXAnim.value != 0f ||
                                rotationYAnim.value != getInitialYRotation(isLeftSide) ||
                                scaleAnim.value != 1f ||
                                elevationAnim.value != 4f
                ) {

                    animationInProgress.value = true
                    try {
                        // Complete animations smoothly rather than abruptly stopping
                        rotationYAnim.animateTo(
                                getInitialYRotation(isLeftSide),
                                animationSpec = KnobAnimations.finishSmoothlySpec
                        )
                        rotationXAnim.animateTo(
                                0f,
                                animationSpec = KnobAnimations.finishSmoothlySpec
                        )
                        scaleAnim.animateTo(1f, animationSpec = KnobAnimations.finishSmoothlySpec)
                        elevationAnim.animateTo(
                                4f,
                                animationSpec = KnobAnimations.finishSmoothlySpec
                        )
                    } finally {
                        animationInProgress.value = false
                    }
                }
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
                                    shadowElevation = elevationAnim.value
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

private fun getInitialYRotation(isLeftSide: Boolean) : Float {
    return if (isLeftSide) 15f else -15f
}

/** Coordinates knob animations to avoid conflicts and ensures smooth transitions */
private class KnobAnimationCoordinator {
    var activeDirection: TuningDirection = TuningDirection.IN_TUNE

    fun updateDirection(direction: TuningDirection) {
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
