package org.samo_lego.katara.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import org.samo_lego.katara.R
import org.samo_lego.katara.ui.components.TuningInfoDisplay
import org.samo_lego.katara.ui.viewmodel.TunerViewModel
import org.samo_lego.katara.util.TuningDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KataraApp(tunerViewModel: TunerViewModel) {
    // Collect states from the tuner view model
    val uiState by tunerViewModel.uiState.collectAsState()
    val activeString = uiState.activeString
    val currentNote = uiState.currentNote
    val isListening = uiState.isListening

    // Calculate tuning direction and value from detected note
    val tuningDirection = currentNote?.tuningDirection ?: TuningDirection.IN_TUNE

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { AppTopBar() },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // Main content layout
            Column(
                    modifier =
                            Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp)
            ) {
                // Guitar visualization (top 3/4)
                GuitarComponent(
                        activeString = activeString,
                        tuningDirection = tuningDirection,
                        modifier = Modifier.weight(3f).fillMaxWidth()
                )

                // Tuning info display (bottom 1/4)
                when {
                    currentNote != null && isListening -> {
                        TuningInfoDisplay(
                                noteName = currentNote.fullNoteName,
                                frequency = currentNote.frequency,
                                cents = currentNote.centsDifference,
                                modifier = Modifier.weight(1f)
                        )
                    }
                    isListening -> {
                        TuningWaitingDisplay(modifier = Modifier.weight(1f))
                    }
                    else -> {
                        EmptyPlaceholder(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar() {
    CenterAlignedTopAppBar(
            title = {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.app_name))
                    Text(text = "To my ❤, Rebeka", style = MaterialTheme.typography.bodySmall)
                }
            },
            colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
    )
}

@Composable
private fun TuningWaitingDisplay(modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
        ) {
            Text(
                    text = "Waiting for sound ...",
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

@Composable
private fun EmptyPlaceholder(modifier: Modifier = Modifier) {
    // Empty placeholder with same size as the other cards
    Card(modifier = modifier.fillMaxSize()) {}
}
