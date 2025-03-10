package org.samo_lego.katara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.ui.theme.StringHighlight
import org.samo_lego.katara.util.NoteFrequency

/** Renders a highlight overlay for the active string */
@Composable
fun ActiveStringOverlay(
        activeString: NoteFrequency,
        spec: GuitarSpecification,
        modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Calculate scaling factors
        val scalingInfo = calculateCanvasScaling(size.width, size.height, spec)
        val stringPosition = spec.stringPositions[activeString] ?: return@Canvas

        // Calculate scaled positions
        val scaledStart = scalePosition(stringPosition.startX, stringPosition.startY, scalingInfo)

        val scaledMiddle = scalePosition(stringPosition.bottomX, spec.bottomStringY, scalingInfo)

        val scaledEnd = scalePosition(stringPosition.bottomX, spec.viewportHeight, scalingInfo)

        // Draw the main string segment
        drawLine(
                color = StringHighlight,
                start = scaledStart,
                end = scaledMiddle,
                strokeWidth = 3.5f * scalingInfo.scale,
                cap = StrokeCap.Round
        )

        // Draw the bottom segment
        drawLine(
                color = StringHighlight,
                start = scaledMiddle,
                end = scaledEnd,
                strokeWidth = 3.5f * scalingInfo.scale,
                cap = StrokeCap.Round
        )
    }
}

/** Scaling information for the canvas */
private data class CanvasScalingInfo(val scale: Float, val offsetX: Float, val offsetY: Float)

/** Calculate scaling information for the canvas */
private fun calculateCanvasScaling(
        canvasWidth: Float,
        canvasHeight: Float,
        spec: GuitarSpecification
): CanvasScalingInfo {
    val scaleX = canvasWidth / spec.viewportWidth
    val scaleY = canvasHeight / spec.viewportHeight
    val scale = minOf(scaleX, scaleY)

    val offsetX = (canvasWidth - (spec.viewportWidth * scale)) / 2f
    val offsetY = (canvasHeight - (spec.viewportHeight * scale)) / 2f

    return CanvasScalingInfo(scale, offsetX, offsetY)
}

/** Scale a position from SVG coordinates to canvas coordinates */
private fun scalePosition(x: Float, y: Float, scaling: CanvasScalingInfo): Offset {
    return Offset(x = x * scaling.scale + scaling.offsetX, y = y * scaling.scale + scaling.offsetY)
}
