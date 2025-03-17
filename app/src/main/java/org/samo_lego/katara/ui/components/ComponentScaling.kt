package org.samo_lego.katara.ui.components

import androidx.compose.ui.geometry.Offset
import org.samo_lego.katara.model.InstrumentLayoutSpecification

/** Scaling information for the canvas */
data class CanvasScalingInfo(val scale: Float, val offsetX: Float, val offsetY: Float)

/** Calculate scaling information for the canvas */
fun calculateCanvasScaling(
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
fun scalePosition(x: Float, y: Float, scaling: CanvasScalingInfo): Offset {
    return Offset(x = x * scaling.scale + scaling.offsetX, y = y * scaling.scale + scaling.offsetY)
}
