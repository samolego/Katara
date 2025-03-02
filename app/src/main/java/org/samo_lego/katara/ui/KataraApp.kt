package org.samo_lego.katara.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.samo_lego.katara.R
import org.samo_lego.katara.ui.components.TuningInfoDisplay
import org.samo_lego.katara.ui.viewmodel.GuitarTunerViewModel
import org.samo_lego.katara.ui.viewmodel.TunerViewModel
import org.samo_lego.katara.util.TuningDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KataraApp() {
    val guitarViewModel: GuitarTunerViewModel = viewModel()
    val tunerViewModel: TunerViewModel = viewModel()

    // Collect states from the tuner view model
    val activeString by tunerViewModel.activeString.collectAsState()
    val currentNote by tunerViewModel.currentNote.collectAsState()
    val isListening by tunerViewModel.isListening.collectAsState()

    // Calculate tuning direction and value from detected note
    val tuningDirection = currentNote?.tuningDirection ?: TuningDirection.IN_TUNE
    val tuningValue = calculateTuningValue(currentNote?.centsDifference ?: 0.0)

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(R.string.app_name)) },
                        colors =
                                TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                ) {
                    GuitarComponent(
                            activeString = activeString,
                            tuningDirection = tuningDirection,
                            tuningValue = tuningValue,
                            isListening = isListening,
                            onActiveStringChange = { string ->
                                // When user manually selects a string
                                tunerViewModel.selectString(string)
                            },
                            onTuningValueChange = { value ->
                                guitarViewModel.updateTuningValue(value)
                            },
                            onToggleListen = {
                                Log.d("KataraApp", "Tuner toggled")
                                tunerViewModel.toggleTuner()
                            }
                    )
                }

                // Add note detection info display at the bottom if we have a note
                currentNote?.let { note ->
                    TuningInfoDisplay(
                            noteName = note.fullNoteName,
                            frequency = note.frequency,
                            cents = note.centsDifference,
                            tuningDirection = note.tuningDirection,
                            modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calculate a tuning value between -1.0 and 1.0 from cents difference This scales the cents value
 * to a range suitable for the slider
 */
private fun calculateTuningValue(centsDifference: Double): Float {
    // Normalize cents to a -1.0 to 1.0 range
    // Typically, ±50 cents is considered significantly out of tune
    return (centsDifference / 50.0).coerceIn(-1.0, 1.0).toFloat()
}
