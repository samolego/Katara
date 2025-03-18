package org.samo_lego.katara.ui.components.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import org.samo_lego.katara.instrument.InstrumentLayoutSpecification
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.ui.util.calculateCanvasScaling
import org.samo_lego.katara.ui.util.scalePosition
import org.samo_lego.katara.ui.theme.StringHighlight

/** Renders a highlight overlay for the active string */
@Composable
fun ActiveStringOverlay(
    activeString: NoteFrequency,
    spec: InstrumentLayoutSpecification,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Calculate scaling factors
        val scalingInfo = calculateCanvasScaling(size.width, size.height, spec)
        val stringPosition = spec.stringDataMap[activeString]?.stringPosition ?: return@Canvas

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
