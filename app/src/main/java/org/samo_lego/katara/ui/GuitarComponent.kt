package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.samo_lego.katara.ui.components.GuitarWithTuners
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection

@Composable
fun GuitarComponent(
        activeString: NoteFrequency?,
        tuningDirection: TuningDirection,
        modifier: Modifier = Modifier
) {
    // Use aspect ratio to maintain guitar proportions
    GuitarWithTuners(
            activeString = activeString,
            tuningDirection = tuningDirection,
            modifier = modifier.fillMaxSize().aspectRatio(0.5f)
    )
}
