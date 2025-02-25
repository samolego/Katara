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
import org.samo_lego.katara.util.GuitarString
import org.samo_lego.katara.util.TuningDirection

@Composable
fun GuitarWithTuners(
    activeString: GuitarString?,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier,
    spec: GuitarSpecification = GuitarSpecification.STANDARD_6_STRING,
    imageSize: Float = 0.7f
) {
    var size = remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(imageSize)
                .padding(8.dp)
                .onSizeChanged { size.value = it }
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
                painter = painterResource(id = R.drawable.guitar),
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
    activeString: GuitarString?,
    tuningDirection: TuningDirection,
    spec: GuitarSpecification,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Left side tuners
        spec.stringPositions
            .filter { (string, _) -> string in listOf(GuitarString.D3, GuitarString.A2, GuitarString.E2) }
            .forEach { (string, position) ->
                val x = position.startX * scale + offsetX
                val y = position.startY * scale + offsetY

                LeftTunerWithNote(
                    tuner = TunerState(
                        note = string.fullNoteName(),
                        rotation = getKnobRotation(string, activeString, tuningDirection)
                    ),
                    onRotationChange = {},
                    modifier = Modifier.graphicsLayer {
                        translationX = x
                        translationY = y
                    }
                )
            }

        // Right side tuners
        spec.stringPositions
            .filter { (string, _) -> string in listOf(GuitarString.G3, GuitarString.B3, GuitarString.E4) }
            .forEach { (string, position) ->
                val x = position.startX * scale + offsetX
                val y = position.startY * scale + offsetY

                RightTunerWithNote(
                    tuner = TunerState(
                        note = string.fullNoteName(),
                        rotation = getKnobRotation(string, activeString, tuningDirection)
                    ),
                    onRotationChange = {},
                    modifier = Modifier.graphicsLayer {
                        translationX = x
                        translationY = y
                    }
                )
            }
    }
}

private fun getKnobRotation(
    knobString: GuitarString,
    activeString: GuitarString?,
    tuningDirection: TuningDirection
): Float {
    if (knobString != activeString) return 0f
    return when (tuningDirection) {
        TuningDirection.UP -> 45f
        TuningDirection.DOWN -> -45f
        TuningDirection.NONE -> 0f
    }
}
