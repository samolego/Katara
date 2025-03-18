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
import org.samo_lego.katara.tuner.NoteFrequency
import org.samo_lego.katara.tuner.TunerService
import org.samo_lego.katara.tuner.calculateStringDifference
import org.samo_lego.katara.tuner.TunerState as TunerServiceState

/** ViewModel for the tuner screen */
class TunerViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "TunerViewModel"

    // Consolidated UI state
    data class UiState(
            val isListening: Boolean = false,
            val chosenInstrument: InstrumentLayoutSpecification = InstrumentLayoutSpecification.GUITAR_STANDARD,
            val activeString: NoteFrequency? = null,
            val manualMode: Boolean = false,
            val currentNote: NoteData? = null,
            val errorMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Service
    private val tunerService = TunerService()

    init {
        // Observe tuner service state
        viewModelScope.launch {
            tunerService.tunerState.collectLatest { state -> updateTunerState(state) }
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

    /** Allow toggling manual mode */
    fun toggleManualMode(note: NoteFrequency?) {
        _uiState.update { currentState ->
                val nextMode = (!currentState.manualMode || currentState.activeString != note) && note != null
                val string = if (nextMode) {
                    note
                } else {
                    null
                }
                currentState.copy(manualMode = nextMode, activeString = string)
        }
    }

    /** Process detected note data and update UI state */
    private fun processNoteData(noteData: NoteData?) {
        if (noteData == null) {
            _uiState.update { currentState ->
                currentState.copy(
                        currentNote = null,
                )
            }
            return
        }

        // Update with detected note if we have a matching string
        noteData.closestGuitarString?.let { detectedString ->
            _uiState.update { currentState ->
                val data = if (currentState.manualMode) {
                   calculateStringDifference(noteData, noteData.frequency, currentState.activeString!!)
                } else {
                    noteData
                }
                currentState.copy(
                        activeString = data.closestGuitarString,
                        currentNote = data,
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
