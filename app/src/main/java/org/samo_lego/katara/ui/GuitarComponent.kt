package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.samo_lego.katara.ui.components.GuitarWithTuners
import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.TuningDirection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuitarComponent(
    activeString: InstrumentString?,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .aspectRatio(0.5f)
        ) {
            GuitarWithTuners(activeString = activeString, tuningDirection = tuningDirection)
        }
    }
}
