package org.samo_lego.katara.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.instrument.InstrumentLayoutSpecification

@Composable
fun InstrumentChooseDialog(
    currentInstrument: InstrumentLayoutSpecification,
    onInstrumentSelected: (InstrumentLayoutSpecification) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Instrument") },
        text = {
            LazyColumn {
                items(InstrumentLayoutSpecification.availableInstruments) { instrument ->
                    InstrumentItem(
                        instrument = instrument,
                        isSelected = instrument == currentInstrument,
                        onClick = {
                            onInstrumentSelected(instrument)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InstrumentItem(
    instrument: InstrumentLayoutSpecification,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = instrument.name,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = instrument.stringDataMap.keys.joinToString(", ") { it.fullNoteName },
                fontWeight = FontWeight.Light
            )
        }
    }
}
