package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    modifier: Modifier = Modifier,
) {
    // Create a more stable tuning state with hysteresis
    // This prevents rapid switching when at the threshold boundary
    val stableTuningDirection = remember(cents) {
        val threshold = 15.0
        when {
            abs(cents) < threshold -> TuningDirection.IN_TUNE
            cents > 0 -> TuningDirection.TOO_HIGH
            else -> TuningDirection.TOO_LOW
        }
    }

    // Remember the last tuning advice when not in tune
    val lastTuningAdvice = remember { mutableStateOf("") }
    if (stableTuningDirection != TuningDirection.IN_TUNE) {
        lastTuningAdvice.value = if (stableTuningDirection == TuningDirection.TOO_LOW)
            "Tighten the string" else "Loosen the string"
    }

    // Create a stable tuning advice that doesn't disappear immediately
    val tuningAdvice = remember(cents, lastTuningAdvice.value) {
        derivedStateOf {
            when {
                abs(cents) < 5.0 -> "Perfect! Hold this tune."
                abs(cents) < 15.0 -> "Almost in tune! ${lastTuningAdvice.value} slightly."
                else -> lastTuningAdvice.value
            }
        }
    }

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
            val directionText = when (stableTuningDirection) {
                TuningDirection.TOO_LOW -> "Too Low"
                TuningDirection.IN_TUNE -> "In Tune"
                TuningDirection.TOO_HIGH -> "Too High"
            }

            val directionColor = when (stableTuningDirection) {
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
                    tuningDirection = stableTuningDirection,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }

            // Always show tuning advice, with appropriate message based on how close to in-tune
            /*Text(
                text = tuningAdvice.value,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )*/
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


@Preview
@Composable
fun TuningInfoDisplayPreview() {
    TuningInfoDisplay(
        noteName = "",
        frequency = 440.0,
        cents = 22.0
    )
}
