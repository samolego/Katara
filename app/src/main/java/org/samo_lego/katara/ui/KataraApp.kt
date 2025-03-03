package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.R
import org.samo_lego.katara.ui.components.TuningInfoDisplay
import org.samo_lego.katara.ui.viewmodel.TunerViewModel
import org.samo_lego.katara.util.TuningDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KataraApp(
    tunerViewModel: TunerViewModel,
) {
    // Collect states from the tuner view model
    val activeString by tunerViewModel.activeString.collectAsState()
    val currentNote by tunerViewModel.currentNote.collectAsState()
    val isListening by tunerViewModel.isListening.collectAsState()

    // Calculate tuning direction and value from detected note
    val tuningDirection = currentNote?.tuningDirection ?: TuningDirection.IN_TUNE

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                        title = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(R.string.app_name))
                                Text("To my ❤, Rebeka", style = MaterialTheme.typography.bodySmall)
                            }
                                },
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
                    modifier = Modifier.weight(3f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GuitarComponent(
                        activeString = activeString,
                        tuningDirection = tuningDirection,
                    )
                }



                Box(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                        .padding(4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (currentNote != null && isListening) {
                        TuningInfoDisplay(
                            noteName = currentNote!!.fullNoteName,
                            frequency = currentNote!!.frequency,
                            cents = currentNote!!.centsDifference,
                        )
                    } else if (isListening) {
                        // No note detected, but tuner is active
                        TuningWaitingDisplay(
                        )
                    } else {
                        // Not listening at all - show empty placeholder to maintain layout
                        EmptyPlaceholder()
                    }
                }

            }
        }
    }
}

@Composable
private fun TuningWaitingDisplay(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Waiting for sound...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bend some air by playing a string.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TuningWaitingPreview() {
    TuningWaitingDisplay()
}

@Composable
private fun EmptyPlaceholder(modifier: Modifier = Modifier) {
    // Empty placeholder with same size as the other cards
    Card(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Empty content, just maintains layout space
        }
    }
}
