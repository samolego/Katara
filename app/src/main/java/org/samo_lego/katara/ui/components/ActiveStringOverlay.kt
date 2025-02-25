package org.samo_lego.katara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.ui.theme.StringHighlight
import org.samo_lego.katara.util.GuitarString

@Composable
fun ActiveStringOverlay(
    activeString: GuitarString,
    spec: GuitarSpecification,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val scaleX = canvasWidth / spec.viewportWidth
        val scaleY = canvasHeight / spec.viewportHeight
        val scale = minOf(scaleX, scaleY)

        val offsetX = (canvasWidth - (spec.viewportWidth * scale)) / 2f
        val offsetY = (canvasHeight - (spec.viewportHeight * scale)) / 2f

        val stringPosition = spec.stringPositions[activeString] ?: return@Canvas

        val scaledMainStart = Offset(
            stringPosition.startX * scale + offsetX,
            stringPosition.startY * scale + offsetY
        )
        val scaledMainEnd = Offset(
            stringPosition.bottomX * scale + offsetX,
            spec.bottomStringY * scale + offsetY
        )

        val scaledBottomEnd = Offset(
            stringPosition.bottomX * scale + offsetX,
            spec.viewportHeight * scale + offsetY
        )

        drawLine(
            color = StringHighlight,
            start = scaledMainStart,
            end = scaledMainEnd,
            strokeWidth = 3.5f * scale,
            cap = StrokeCap.Round
        )

        drawLine(
            color = StringHighlight,
            start = scaledMainEnd,
            end = scaledBottomEnd,
            strokeWidth = 3.5f * scale,
            cap = StrokeCap.Round
        )
    }
}
