package org.samo_lego.katara.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.R
import org.samo_lego.katara.ui.theme.StringHighlight
import org.samo_lego.katara.util.GuitarString

/**
 * A composable that displays a guitar with the ability to highlight a specific string. This version
 * properly scales on all screen sizes and uses type-safe GuitarString enum.
 *
 * @param activeString The string to highlight, or null to highlight none
 * @param highlightColor The color to use for highlighting the active string
 * @param modifier Modifier for the composable
 * @param imageSize The maximum size the guitar should take (0f-1f)
 */
@Composable
fun GuitarImage(
        activeString: GuitarString? = null,
        highlightColor: Color = StringHighlight,
        modifier: Modifier = Modifier,
        imageSize: Float = 0.7f
) {
    // Original SVG viewport dimensions
    val svgViewportWidth = 153f
    val svgViewportHeight = 326f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize(imageSize).padding(8.dp)) {
            // Draw the base guitar image
            Image(
                    painter = painterResource(id = R.drawable.guitar),
                    contentDescription = "Guitar Headstock",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
            )

            // If a string is active, draw the highlighted string on top
            if (activeString != null) {
                ActiveString(
                    imageViewWidth = svgViewportWidth,
                    imageViewHeight = svgViewportHeight,
                    highlightColor = highlightColor,
                    activeString = activeString,
                )
            }
        }
    }
}

@Preview
@Composable
fun GuitarImagePreview() {
    GuitarImage(
        activeString = GuitarString.E2
    )
}

@Composable
fun ActiveString(
    imageViewWidth: Float,
    imageViewHeight: Float,
    activeString: GuitarString,
    highlightColor: Color = StringHighlight
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate scaling factors while maintaining the aspect ratio
        val scaleX = canvasWidth / imageViewWidth
        val scaleY = canvasHeight / imageViewHeight
        val scale = minOf(scaleX, scaleY)

        // Calculate offsets to center the drawing
        val offsetX = (canvasWidth - (imageViewWidth * scale)) / 2f
        val offsetY = (canvasHeight - (imageViewHeight * scale)) / 2f

        // Updated string coordinates to match SVG
        val yBottom = 297f
        val stringPaths =
                mapOf(
                        GuitarString.D3 to Pair(Offset(36f, 63f), Offset(69f, yBottom)),
                        GuitarString.A2 to
                                Pair(Offset(36f, 143f), Offset(57f, yBottom)),
                        GuitarString.E2 to
                                Pair(Offset(36f, 223f), Offset(47f, yBottom)),
                        GuitarString.G3 to
                                Pair(Offset(116f, 63f), Offset(80f, yBottom)),
                        GuitarString.B3 to
                                Pair(Offset(116f, 143f), Offset(90f, yBottom)),
                        GuitarString.E4 to
                                Pair(Offset(116f, 223f), Offset(101f, yBottom)),
                )
        // Bottom parallel string coordinates
        val yBottom2 = 325.5f
        val bottomStringPaths =
                mapOf(
                        GuitarString.D3 to Offset(69f, yBottom2),
                        GuitarString.A2 to Offset(57f, yBottom2),
                        GuitarString.E2 to Offset(47f, yBottom2),
                        GuitarString.G3 to Offset(80f, yBottom2),
                        GuitarString.B3 to Offset(90f, yBottom2),
                        GuitarString.E4 to Offset(101f, yBottom2),
                )

        // Get the start and end points for both parts of the active string
        val (mainStart, mainEnd) = stringPaths[activeString] ?: return@Canvas
        val bottomEnd = bottomStringPaths[activeString] ?: return@Canvas

        // Scale the coordinates for the main diagonal part
        val scaledMainStart =
                Offset(mainStart.x * scale + offsetX, mainStart.y * scale + offsetY)
        val scaledMainEnd =
                Offset(mainEnd.x * scale + offsetX, mainEnd.y * scale + offsetY)

        // Scale the coordinates for the bottom parallel part
        val scaledBottomStart =
                Offset(mainEnd.x * scale + offsetX, mainEnd.y * scale + offsetY)
        val scaledBottomEnd =
                Offset(bottomEnd.x * scale + offsetX, bottomEnd.y * scale + offsetY)

        // Draw both parts of the highlighted string
        // Main diagonal part
        drawLine(
                color = highlightColor,
                start = scaledMainStart,
                end = scaledMainEnd,
                strokeWidth = 3.5f * scale,
                cap = StrokeCap.Round
        )

        // Bottom parallel part
        drawLine(
                color = highlightColor,
                start = scaledBottomStart,
                end = scaledBottomEnd,
                strokeWidth = 3.5f * scale,
                cap = StrokeCap.Round
        )
    }
}
