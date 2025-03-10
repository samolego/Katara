package org.samo_lego.katara.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.R
import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.model.StringPosition
import org.samo_lego.katara.util.InstrumentType
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection

/** Scaling information for guitar display */
private data class ScalingInfo(val scale: Float, val offsetX: Float, val offsetY: Float)

@Composable
fun GuitarWithTuners(
        activeString: NoteFrequency?,
        tuningDirection: TuningDirection,
        modifier: Modifier = Modifier,
        spec: GuitarSpecification = GuitarSpecification.STANDARD_6_STRING,
        imageSize: Float = 0.7f
) {
    // Track the component size to calculate scaling
    val size = remember { mutableStateOf(IntSize.Zero) }

    // Calculate scaling info when size changes
    val scalingInfo = remember(size.value) { calculateScaling(size.value, spec) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Container for the guitar and tuners
        Box(
                modifier =
                        Modifier.fillMaxSize(imageSize).padding(8.dp).onSizeChanged {
                            size.value = it
                        }
        ) {
            // Base guitar image
            Image(
                    painter = painterResource(id = R.drawable.instrument_guitar),
                    contentDescription = "Guitar Headstock",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
            )

            // Active string highlight overlay
            if (activeString != null) {
                ActiveStringOverlay(
                        activeString = activeString,
                        spec = spec,
                        modifier = Modifier.fillMaxSize()
                )
            }

            // Tuners overlay
            TunersLayout(
                    activeString = activeString,
                    tuningDirection = tuningDirection,
                    spec = spec,
                    getStringNumber = { InstrumentType.GUITAR_STANDARD.getStringNumber(it) },
                    scalingInfo = scalingInfo,
                    modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Calculate scaling information based on component size and specification */
private fun calculateScaling(size: IntSize, spec: GuitarSpecification): ScalingInfo {
    val width = size.width.toFloat()
    val height = size.height.toFloat()

    val scaleX = width / spec.viewportWidth
    val scaleY = height / spec.viewportHeight
    val scale = minOf(scaleX, scaleY)

    val offsetX = (width - (spec.viewportWidth * scale)) / 2f
    val offsetY = (height - (spec.viewportHeight * scale)) / 2f

    return ScalingInfo(scale, offsetX, offsetY)
}

@Composable
private fun TunersLayout(
        activeString: NoteFrequency?,
        tuningDirection: TuningDirection,
        spec: GuitarSpecification,
        getStringNumber: (NoteFrequency) -> Int,
        scalingInfo: ScalingInfo,
        modifier: Modifier = Modifier
) {
    // Size of the tuner knob in SVG coordinates
    val tunerRadius = 12f

    Box(modifier = modifier) {
        // Display each tuner knob
        spec.stringPositions.forEach { (noteFreq, position) ->
            TunerKnobForString(
                    noteFreq = noteFreq,
                    position = position,
                    tunerRadius = tunerRadius,
                    getStringNumber = getStringNumber,
                    xOffset = spec.knobsXOffsets[noteFreq] ?: 0f,
                    scalingInfo = scalingInfo,
                    isActive = noteFreq == activeString,
                    tuningDirection =
                            if (noteFreq == activeString) tuningDirection
                            else TuningDirection.IN_TUNE
            )
        }
    }
}

/** Position and render a single tuner knob for a string */
@Composable
private fun TunerKnobForString(
        noteFreq: NoteFrequency,
        position: StringPosition,
        tunerRadius: Float,
        getStringNumber: (NoteFrequency) -> Int,
        xOffset: Float,
        scalingInfo: ScalingInfo,
        isActive: Boolean,
        tuningDirection: TuningDirection
) {
    // Calculate position
    val (scale, offsetX, offsetY) = scalingInfo
    val x = (position.startX - tunerRadius + xOffset) * scale + offsetX
    val y = (position.startY - tunerRadius) * scale + offsetY

    // Determine if this is a left-side knob based on string number
    val isLeftSide = getStringNumber(noteFreq) > 3

    // Create and position the tuner knob
    GuitarTunerKnob(
            tuner =
                    TunerState(
                            note = noteFreq.fullNoteName,
                            tuningDirection = tuningDirection,
                            isActive = isActive,
                    ),
            isLeftSide = isLeftSide,
            onRotationChange = {},
            modifier =
                    Modifier.graphicsLayer {
                        translationX = x
                        translationY = y
                    }
    )
}
