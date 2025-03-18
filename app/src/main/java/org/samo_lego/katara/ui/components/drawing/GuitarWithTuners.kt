package org.samo_lego.katara.ui.components.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.instrument.InstrumentLayoutSpecification
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.tuner.TuningDirection

/** Scaling information for guitar display */
private data class ScalingInfo(val scale: Float, val offsetX: Float, val offsetY: Float)

@Composable
fun GuitarWithTuners(
    activeString: NoteFrequency?,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier,
    layoutSpec: InstrumentLayoutSpecification,
    imageSize: Float = 0.7f,
    manualModeClick: (NoteFrequency) -> Unit = {},
) {
    // Track the component size to calculate scaling
    val size = remember { mutableStateOf(IntSize.Zero) }

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
                    painter = painterResource(id = layoutSpec.drawableId),
                    contentDescription = "Guitar Headstock",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
            )

            // Active string highlight overlay
            if (activeString != null) {
                ActiveStringOverlay(
                        activeString = activeString,
                        spec = layoutSpec,
                        modifier = Modifier.fillMaxSize()
                )
            }

            // Tuners overlay
            TunersLayout(
                activeString = activeString,
                tuningDirection = tuningDirection,
                spec = layoutSpec,
                manualModeClick = manualModeClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Calculate scaling information based on component size and specification */
private fun calculateScaling(size: IntSize, spec: InstrumentLayoutSpecification): ScalingInfo {
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
    spec: InstrumentLayoutSpecification,
    manualModeClick: (NoteFrequency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Display each tuner knob
        spec.stringDataMap.forEach { (noteFreq, stringData) ->
            GuitarKnob(
                noteFreq = noteFreq,
                data = stringData,
                modifier = modifier,
                spec = spec,
                isLeftSide = stringData.knobOffset < 0.0f,
                isActive = noteFreq == activeString,
                tuningDirection = if (noteFreq == activeString) tuningDirection
                                  else TuningDirection.IN_TUNE,
                onClick = {
                    manualModeClick(noteFreq)
                },
            )
        }
    }
}

@Preview
@Composable
fun GuitarPreview() {
    GuitarWithTuners(
        activeString = NoteFrequency.G3,
        tuningDirection = TuningDirection.TOO_HIGH,
        layoutSpec = InstrumentLayoutSpecification.GUITAR_STANDARD,
        modifier = Modifier.fillMaxSize().aspectRatio(0.5f),

    )
}

@Preview
@Composable
fun UkulelePreview() {
    GuitarWithTuners(
        activeString = NoteFrequency.G4,
        tuningDirection = TuningDirection.TOO_HIGH,
        layoutSpec = InstrumentLayoutSpecification.UKULELE_STANDARD,
    )
}
