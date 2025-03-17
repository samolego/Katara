package org.samo_lego.katara.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.samo_lego.katara.model.InstrumentLayoutSpecification
import org.samo_lego.katara.tuner.NoteData
import org.samo_lego.katara.tuner.TunerService
import org.samo_lego.katara.tuner.TunerServiceState as TunerServiceState
import org.samo_lego.katara.util.TunerState
import org.samo_lego.katara.util.InstrumentNotes
import org.samo_lego.katara.util.NoteFrequency
import org.samo_lego.katara.util.TuningDirection

/** ViewModel for the tuner screen */
class TunerViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "TunerViewModel"

    // Consolidated UI state
    data class UiState(
            val isListening: Boolean = false,
            val chosenInstrument: InstrumentLayoutSpecification = InstrumentLayoutSpecification.GUITAR_STANDARD,
            val activeString: NoteFrequency? = null,
            val currentNote: NoteData? = null,
            val tunerKnobsState: Map<NoteFrequency, TunerState> = emptyMap(),
            val errorMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState(tunerKnobsState = createInitialKnobsState()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Service
    private val tunerService = TunerService()

    init {
        // Observe tuner service state
        viewModelScope.launch {
            tunerService.tunerServiceState.collectLatest { state -> updateTunerState(state) }
        }

        // Observe note data from the tuner service
        viewModelScope.launch {
            tunerService.currentNoteData.collectLatest { noteData -> processNoteData(noteData) }
        }
    }

    /** Update UI state based on tuner service state */
    private fun updateTunerState(state: TunerServiceState) {
        _uiState.update { currentState ->
            currentState.copy(
                    isListening = state == TunerServiceState.Active,
                    errorMessage = if (state is TunerServiceState.Error) state.message else null
            )
        }

        // Log errors
        if (state is TunerServiceState.Error) {
            Log.e("$tag#updateTunerState", "Tuner error: ${state.message}")
        }
    }

    /** Update the chosen instrument */
    fun updateChosenInstrument(instrument: InstrumentLayoutSpecification) {
        _uiState.update { currentState ->
            currentState.copy(chosenInstrument = instrument)
        }
    }

    /** Process detected note data and update UI state */
    private fun processNoteData(noteData: NoteData?) {
        if (noteData == null) {
            _uiState.update { currentState ->
                currentState.copy(
                        activeString = null,
                        currentNote = null,
                        tunerKnobsState = resetAllKnobStates(currentState.tunerKnobsState)
                )
            }
            return
        }

        // Update with detected note if we have a matching string
        noteData.closestGuitarString?.let { detectedString ->
            _uiState.update { currentState ->
                currentState.copy(
                        activeString = detectedString,
                        currentNote = noteData,
                        tunerKnobsState =
                                updateKnobsForDetectedNote(
                                        currentState.tunerKnobsState,
                                        detectedString,
                                        noteData.tuningDirection
                                )
                )
            }

            // Log the detection
            Log.d(
                    "$tag#processNoteData",
                    "Detected ${noteData.fullNoteName} (${noteData.frequency}Hz), " +
                            "closest to ${detectedString.fullNoteName}, " +
                            "cents off: ${noteData.centsDifference.toInt()}, " +
                            "direction: ${noteData.tuningDirection}"
            )
        }
    }

    /** Reset all knob states to inactive */
    private fun resetAllKnobStates(
            currentKnobsState: Map<NoteFrequency, TunerState>
    ): Map<NoteFrequency, TunerState> {
        return currentKnobsState.mapValues { (_, state) ->
            state.copy(isActive = false, tuningDirection = TuningDirection.IN_TUNE)
        }
    }

    /** Update knob states based on detected note */
    private fun updateKnobsForDetectedNote(
            currentKnobsState: Map<NoteFrequency, TunerState>,
            activeString: NoteFrequency,
            tuningDirection: TuningDirection
    ): Map<NoteFrequency, TunerState> {
        return currentKnobsState.mapValues { (string, state) ->
            val isActive = string == activeString
            if (isActive || state.isActive) {
                state.copy(
                        isActive = isActive,
                        tuningDirection = if (isActive) tuningDirection else TuningDirection.IN_TUNE
                )
            } else {
                state // No change needed
            }
        }
    }

    /** Create the initial state for all tuner knobs */
    private fun createInitialKnobsState(): Map<NoteFrequency, TunerState> {
        return InstrumentNotes.GUITAR_NOTES.notes.associateWith { note ->
            TunerState(
                    note = note.fullNoteName,
                    isActive = false,
                    tuningDirection = TuningDirection.IN_TUNE
            )
        }
    }

    /** Start the tuner */
    fun startTuner() {
        tunerService.start()
    }

    /** Stop the tuner */
    fun stopTuner() {
        tunerService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tunerService.stop()
    }
}
