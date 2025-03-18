package org.samo_lego.katara.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import org.samo_lego.katara.ui.theme.TuneOk
import org.samo_lego.katara.ui.theme.TuneTooHigh
import org.samo_lego.katara.ui.theme.TuneTooLow
import org.samo_lego.katara.tuner.TuningDirection

@Composable
fun TuningInfoDisplay(
        noteName: String,
        frequency: Double,
        cents: Double,
        modifier: Modifier = Modifier,
) {
    // Create a more stable tuning state with hysteresis
    // This prevents rapid switching when at the threshold boundary
    val stableTuningDirection =
            remember(cents) {
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
        lastTuningAdvice.value =
                if (stableTuningDirection == TuningDirection.TOO_LOW) "Tighten the string"
                else "Loosen the string"
    }
    Card(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            Text(text = "%.2f Hz".format(frequency), style = MaterialTheme.typography.titleMedium)

            // Direction text
            val directionText =
                    when (stableTuningDirection) {
                        TuningDirection.TOO_LOW -> "Too Low"
                        TuningDirection.IN_TUNE -> "In Tune"
                        TuningDirection.TOO_HIGH -> "Too High"
                    }

            val directionColor =
                    when (stableTuningDirection) {
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
                // Visual indicator showing how far from in tune
                TuningMeter(
                        cents = cents,
                        tuningDirection = stableTuningDirection,
                        modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
            }
            Text(text = "${(cents / 10).toInt()}", style = MaterialTheme.typography.bodyMedium)
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

    // Apply exponential smoothing to the cents value
    val smoothedCents = remember { mutableDoubleStateOf(cents) }
    val smoothingFactor = if (isInTune) 0.5 else 0.3

    // Update the smoothed value
    smoothedCents.doubleValue = smoothedCents.doubleValue * (1 - smoothingFactor) + cents * smoothingFactor

    // Define a spring-based animation spec for smoother transitions
    val animSpec: AnimationSpec<Float> =
            SpringSpec(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = if (isInTune) Spring.StiffnessLow else Spring.StiffnessMediumLow,
                    visibilityThreshold = 0.01f
            )

    // Animate the progress with the spring animation
    val progress by
            animateFloatAsState(
                    targetValue =
                            ((50.0 + smoothedCents.doubleValue) / 100.0).coerceIn(0.0, 1.0).toFloat(),
                    animationSpec = animSpec,
                    label = "tuning-progress"
            )

    LinearProgressIndicator(
            progress = { progress },
            color =
                    when {
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
    TuningInfoDisplay(noteName = "A", frequency = 440.0, cents = 22.0)
}
