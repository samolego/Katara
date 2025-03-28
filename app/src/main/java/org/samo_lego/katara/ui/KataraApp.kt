package org.samo_lego.katara.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.samo_lego.katara.R
import org.samo_lego.katara.data.PreferencesManager
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.tuner.TuningDirection
import org.samo_lego.katara.ui.components.InstrumentChooseDialog
import org.samo_lego.katara.ui.components.TuningInfoDisplay
import org.samo_lego.katara.ui.components.drawing.GuitarWithTuners
import org.samo_lego.katara.ui.viewmodel.TunerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KataraApp(
        tunerViewModel: TunerViewModel,
        preferencesManager: PreferencesManager,
) {
    // Collect states from the tuner view model
    val uiState by tunerViewModel.uiState.collectAsState()
    val activeString = uiState.activeString
    val manualMode = uiState.manualMode
    val currentNote = uiState.currentNote
    val isListening = uiState.isListening

    val showDedicationText by
            preferencesManager.dedicationTextVisibility.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    var showInstrumentChooseDialog = remember { mutableStateOf(false) }

    // Calculate tuning direction and value from detected note
    val tuningDirection = currentNote?.tuningDirection ?: TuningDirection.IN_TUNE

    Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                        manualMode = manualMode,
                        showDedicationText = showDedicationText,
                        onDedicationTextClick = { enable ->
                            coroutineScope.launch {
                                preferencesManager.updateDedicationTextVisibility(
                                        !showDedicationText || enable
                                )
                            }
                        },
                        onManualModeToggle = {
                            tunerViewModel.toggleManualMode(if (it) NoteFrequency.A4 else null)
                        },
                        openInstrumentChooseDialog = { showInstrumentChooseDialog.value = true }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            // Main content layout
            Column(
                    modifier =
                            Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp)
            ) {
                // Guitar visualization (top 3/4)
                GuitarWithTuners(
                        activeString = activeString,
                        tuningDirection = tuningDirection,
                        layoutSpec = uiState.chosenInstrument,
                        modifier = Modifier.weight(3f).fillMaxSize().aspectRatio(0.5f),
                        manualModeClick = { tunerViewModel.toggleManualMode(it) }
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
            if (showInstrumentChooseDialog.value) {
                InstrumentChooseDialog(
                        currentInstrument = uiState.chosenInstrument,
                        onInstrumentSelected = { tunerViewModel.updateChosenInstrument(it) },
                        onDismiss = { showInstrumentChooseDialog.value = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AppTopBar(
    manualMode: Boolean,
    showDedicationText: Boolean,
    onDedicationTextClick: (Boolean) -> Unit,
    onManualModeToggle: (Boolean) -> Unit,
    openInstrumentChooseDialog: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
            title = {
                Column(
                        modifier =
                                Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.combinedClickable(
                            onClick = { onDedicationTextClick(true) },
                            onLongClick = { onDedicationTextClick(false) }
                        )
                    )
                    if (showDedicationText) {
                        Text(text = "To my ❤, Rebeka",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            navigationIcon = {
                Column(modifier = Modifier.padding(8.dp)) {
                    Switch(checked = manualMode, onCheckedChange = { onManualModeToggle(it) })
                }
            },
            colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
            actions = {
                IconButton(onClick = { openInstrumentChooseDialog() }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }
            }
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
