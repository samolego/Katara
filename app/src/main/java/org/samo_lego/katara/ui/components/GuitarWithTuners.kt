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
import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.TuningDirection

@Composable
fun GuitarWithTuners(
        activeString: InstrumentString?,
        tuningDirection: TuningDirection,
        modifier: Modifier = Modifier,
        spec: GuitarSpecification = GuitarSpecification.STANDARD_6_STRING,
        imageSize: Float = 0.7f
) {
    var size = remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
                modifier =
                        Modifier.fillMaxSize(imageSize).padding(8.dp).onSizeChanged {
                            size.value = it
                        }
        ) {
            val width = size.value.width.toFloat()
            val height = size.value.height.toFloat()

            // Calculate scale factors only when size changes
            val scaleX = width / spec.viewportWidth
            val scaleY = height / spec.viewportHeight
            val scale = minOf(scaleX, scaleY)

            val offsetX = (width - (spec.viewportWidth * scale)) / 2f
            val offsetY = (height - (spec.viewportHeight * scale)) / 2f

            // Base guitar image
            Image(
                    painter = painterResource(id = R.drawable.guitar_standard),
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
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TunersLayout(
        activeString: InstrumentString?,
        tuningDirection: TuningDirection,
        spec: GuitarSpecification,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        modifier: Modifier = Modifier
) {
    // Size of the tuner knob in SVG coordinates
    val tunerRadius = 12f

    Box(modifier = modifier) {
        // Left side tuners
        spec.stringPositions.filter { (string, _) -> string.stringNumber > 3 }.forEach {
                (string, position) ->
            val x =
                    (position.startX - tunerRadius + spec.knobsXOffsets[string]!!) * scale +
                            offsetX // Subtract radius to center horizontally
            val y =
                    (position.startY - tunerRadius) * scale +
                            offsetY // Subtract radius to center vertically

            GuitarTunerKnob(
                    tuner =
                            TunerState(
                                    note = string.fullNoteName(),
                                    tuningDirection = tuningDirection
                            ),
                    isLeftSide = true,
                    onRotationChange = {},
                    modifier =
                            Modifier.graphicsLayer {
                                translationX = x
                                translationY = y
                            }
            )
        }

        // Right side tuners
        spec.stringPositions.filter { (string, _) -> string.stringNumber <= 3 }.forEach {
                (string, position) ->
            val x =
                    (position.startX - tunerRadius + spec.knobsXOffsets[string]!!) * scale +
                            offsetX // Subtract radius to center horizontally
            val y =
                    (position.startY - tunerRadius) * scale +
                            offsetY // Subtract radius to center vertically

            GuitarTunerKnob(
                    tuner =
                            TunerState(
                                    note = string.fullNoteName(),
                                    tuningDirection = tuningDirection
                            ),
                    isLeftSide = false,
                    onRotationChange = {},
                    modifier =
                            Modifier.graphicsLayer {
                                translationX = x
                                translationY = y
                            }
            )
        }
    }
}

