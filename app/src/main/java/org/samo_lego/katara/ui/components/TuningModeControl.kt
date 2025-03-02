package org.samo_lego.katara.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class TuningMode {
    AUTO, MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningModeControl(
    mode: TuningMode,
    isListening: Boolean,
    onModeChange: (TuningMode) -> Unit,
    onToggleListen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = mode == TuningMode.AUTO,
                onClick = { onModeChange(TuningMode.AUTO) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.weight(1f)
            ) {
                Text("Auto")
            }

            SegmentedButton(
                selected = mode == TuningMode.MANUAL,
                onClick = { onModeChange(TuningMode.MANUAL) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.weight(1f)
            ) {
                Text("Manual")
            }
        }

        // Listen toggle button for auto mode
        if (mode == TuningMode.AUTO) {
            Button(
                onClick = onToggleListen,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop listening" else "Start listening"
                    )
                    Text(if (isListening) "Stop Listening" else "Start Listening")
                }
            }
        }
    }
}
