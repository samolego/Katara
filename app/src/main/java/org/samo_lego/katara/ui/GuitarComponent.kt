package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.ui.components.GuitarWithTuners
import org.samo_lego.katara.util.GuitarString
import org.samo_lego.katara.util.TuningDirection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuitarComponent(
    activeString: GuitarString?,
    tuningDirection: MutableState<TuningDirection>,
    onActiveStringChange: (GuitarString) -> Unit,
    modifier: Modifier = Modifier
) {
    var tuningValue = remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // Guitar display with highlighted string
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            GuitarWithTuners(
                activeString = activeString,
                tuningDirection = tuningDirection.value
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // String selection buttons
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GuitarString.entries.forEach { guitarString ->
                    Button(
                        onClick = { onActiveStringChange(guitarString) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeString == guitarString)
                                Color(0xFFFF9800)
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(guitarString.fullNoteName(), textAlign = TextAlign.Center)
                    }
                }
            }

            // Display which string is active
            activeString?.let {
                Text(
                    text = "String ${it.stringNumber}: ${it.fullNoteName()}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = tuningValue.value,
                    onValueChange = { newValue ->
                        tuningValue.value = newValue
                        tuningDirection.value = when {
                            newValue > 0 -> TuningDirection.UP
                            newValue < 0 -> TuningDirection.DOWN
                            else -> TuningDirection.NONE
                        }
                    },
                    valueRange = -1f..1f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
