package org.samo_lego.katara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import org.samo_lego.katara.model.InstrumentLayoutSpecification
import org.samo_lego.katara.model.StringData
import org.samo_lego.katara.model.StringPosition
import org.samo_lego.katara.ui.theme.StringHighlight
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection

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

@Composable
fun GuitarKnob(
    noteFreq: NoteFrequency,
    data: StringData,
    spec: InstrumentLayoutSpecification,
    tunerRadius: Float,
    isLeftSide: Boolean,
    xOffset: Float,
    isActive: Boolean,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scalingInfo = calculateCanvasScaling(this.constraints.maxWidth.toFloat(), this.constraints.maxHeight.toFloat(), spec, offsetX = data.knobOffset)
        val center = scalePosition(data.stringPosition.startX, data.stringPosition.startY, scalingInfo)

        val buttonSize = 25f * scalingInfo.scale
        val buttonSizeDp = with(density) { buttonSize.toDp() }

        // Position the button at the calculated center position
        val xOffset = with(density) { (center.x - buttonSize / 2).toDp() }
        val yOffset = with(density) { (center.y - buttonSize / 2).toDp() }

        Box(
            modifier = Modifier
                .offset(x = xOffset, y = yOffset)
                .size(buttonSizeDp)
                .clip(CircleShape)
                .background(StringHighlight)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Add text or any other composable you want inside the button
            Text(
                text = noteFreq.toString(),
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

/** Scaling information for the canvas */
private data class CanvasScalingInfo(val scale: Float, val offsetX: Float, val offsetY: Float)

/** Calculate scaling information for the canvas */
private fun calculateCanvasScaling(
        canvasWidth: Float,
        canvasHeight: Float,
        spec: InstrumentLayoutSpecification,
        offsetX: Float = 0f,
): CanvasScalingInfo {
    val scaleX = canvasWidth / spec.viewportWidth
    val scaleY = canvasHeight / spec.viewportHeight
    val scale = minOf(scaleX, scaleY)

    val offsetX = (canvasWidth - (spec.viewportWidth * scale)) / 2f + offsetX * scale
    val offsetY = (canvasHeight - (spec.viewportHeight * scale)) / 2f

    return CanvasScalingInfo(scale, offsetX, offsetY)
}

/** Scale a position from SVG coordinates to canvas coordinates */
private fun scalePosition(x: Float, y: Float, scaling: CanvasScalingInfo): Offset {
    return Offset(x = x * scaling.scale + scaling.offsetX, y = y * scaling.scale + scaling.offsetY)
}
