package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.ui.components.GuitarImage
import org.samo_lego.katara.util.GuitarString

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun GuitarComponent() {
    var activeString = remember { mutableStateOf<GuitarString?>(null) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        // Guitar display with highlighted string
        GuitarImage(
                activeString = activeString.value,
        )

        // String selection buttons
        FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GuitarString.entries.forEach { guitarString ->
                Button(
                        onClick = {
                            activeString.value = if (activeString.value == guitarString) null else guitarString
                        },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor =
                                                if (activeString.value == guitarString) Color(0xFFFF9800)
                                                else MaterialTheme.colorScheme.primary
                                ),
                        modifier = Modifier.padding(4.dp)
                ) { Text(guitarString.fullNoteName(), textAlign = TextAlign.Center) }
            }
        }

        // Display which string is active
        activeString.value?.let {
            Text(
                    text = "String ${it.stringNumber}: ${it.fullNoteName()}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
