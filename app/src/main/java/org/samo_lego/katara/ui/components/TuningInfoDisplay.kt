package org.samo_lego.katara.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
import org.samo_lego.katara.util.TuningDirection
import kotlin.math.abs

@Composable
fun TuningInfoDisplay(
    noteName: String,
    frequency: Double,
    cents: Double,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Note name in large text
            Text(
                text = noteName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Frequency with Hz unit
            Text(
                text = "%.2f Hz".format(frequency),
                style = MaterialTheme.typography.titleMedium
            )

            // Direction text
            val directionText = when (tuningDirection) {
                TuningDirection.TOO_LOW -> "Too Low"
                TuningDirection.IN_TUNE -> "In Tune"
                TuningDirection.TOO_HIGH -> "Too High"
            }

            val directionColor = when (tuningDirection) {
                TuningDirection.TOO_LOW -> TuneTooLow
                TuningDirection.IN_TUNE -> TuneOk
                TuningDirection.TOO_HIGH -> TuneTooHigh
            }

            Text(
                text = directionText,
                style = MaterialTheme.typography.bodyLarge,
                color = directionColor,
                fontWeight = FontWeight.Bold
            )

            // Cents display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cents: ${cents.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Visual indicator showing how far from in tune
                TuningMeter(
                    cents = cents,
                    tuningDirection = tuningDirection,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }

            // Tuning advice
            AnimatedVisibility(
                visible = tuningDirection != TuningDirection.IN_TUNE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = if (tuningDirection == TuningDirection.TOO_LOW)
                        "Tighten the string" else "Loosen the string",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun TuningMeter(
    cents: Double,
    tuningDirection: TuningDirection,
    modifier: Modifier = Modifier
) {
    val isInTune = abs(cents) < 5.0

    // Animate the progress
    val progress by animateFloatAsState(
        targetValue = ((50.0 + cents) / 100.0).coerceIn(0.0, 1.0).toFloat(),
        label = "tuning-progress"
    )

    LinearProgressIndicator(
        progress = { progress },
        color = when {
            isInTune -> TuneOk
            tuningDirection == TuningDirection.TOO_LOW -> TuneTooLow
            else -> TuneTooHigh
        },
        trackColor = Color.LightGray.copy(alpha = 0.3f),
        modifier = modifier
    )
}
