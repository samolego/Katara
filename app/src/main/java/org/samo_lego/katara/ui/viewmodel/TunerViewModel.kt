package org.samo_lego.katara.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.samo_lego.katara.tuner.NoteData
import org.samo_lego.katara.tuner.TunerService
import org.samo_lego.katara.ui.components.TunerState
import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.InstrumentType
import org.samo_lego.katara.util.Logger
import org.samo_lego.katara.util.TuningDirection
import org.samo_lego.katara.tuner.TunerState as TunerServiceState

class TunerViewModel(application: Application) : AndroidViewModel(application) {
    // Tuner service
    private val tunerService = TunerService()

    // UI state for the tuner knobs
    private val _tunerKnobsState = MutableStateFlow(createInitialKnobsState())
    val tunerKnobsState: StateFlow<Map<InstrumentString, TunerState>> =
            _tunerKnobsState.asStateFlow()

    // Currently active string
    private val _activeString = MutableStateFlow<InstrumentString?>(null)
    val activeString: StateFlow<InstrumentString?> = _activeString.asStateFlow()

    // Note being detected
    private val _currentNote = MutableStateFlow<NoteData?>(null)
    val currentNote: StateFlow<NoteData?> = _currentNote.asStateFlow()

    // Tuner service state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    init {
        // Observe tuner service state
        viewModelScope.launch {
            tunerService.tunerState.collectLatest { state ->
                _isListening.value = state == TunerServiceState.Active

                if (state is TunerServiceState.Error) {
                    Logger.e("Tuner error: ${state.message}")
                }
            }
        }

        // Observe note data from the tuner service
        viewModelScope.launch {
            tunerService.currentNoteData.collectLatest { noteData -> processNoteData(noteData) }
        }
    }

    /** Process detected note data and update UI state */
    private fun processNoteData(noteData: NoteData?) {
        _currentNote.value = noteData

        // When no note data is detected (silence), deactivate the active string
        if (noteData == null && _activeString.value != null) {
            _activeString.value = null
            resetAllKnobStates()
            return
        }

        noteData?.let { note ->
            // Filter out notes that are too soft or uncertain by checking probability
            note.closestGuitarString?.let { instrumentString ->
                // Update active string
                _activeString.value = instrumentString

                // Update knob states for all strings
                updateKnobsForDetectedNote(instrumentString, note.tuningDirection)

                // Log the detection
                Logger.d(
                        "Detected ${note.fullNoteName} (${note.frequency}Hz), closest to ${instrumentString.fullNoteName()}, " +
                                "cents off: ${note.centsDifference.toInt()}, direction: ${note.tuningDirection}"
                )
            }
        }
    }

    /** Reset all knob states to inactive */
    private fun resetAllKnobStates() {
        val updatedKnobsState = _tunerKnobsState.value.toMutableMap()

        // Reset all knobs to inactive state
        InstrumentType.GUITAR_STANDARD.strings.forEach { string ->
            val currentState = updatedKnobsState[string] ?: TunerState(
                note = string.fullNoteName(),
                isActive = false,
                tuningDirection = TuningDirection.IN_TUNE
            )

            if (currentState.isActive) {
                updatedKnobsState[string] = currentState.copy(
                    isActive = false,
                    tuningDirection = TuningDirection.IN_TUNE
                )
            }
        }

        _tunerKnobsState.value = updatedKnobsState
    }

    /** Update knob states based on detected note */
    private fun updateKnobsForDetectedNote(
            activeString: InstrumentString,
            tuningDirection: TuningDirection
    ) {
        val updatedKnobsState = _tunerKnobsState.value.toMutableMap()

        // Update all knobs
        InstrumentType.GUITAR_STANDARD.strings.forEach { string ->
            val isActive = string == activeString
            val currentState =
                    updatedKnobsState[string]
                            ?: TunerState(
                                    note = string.fullNoteName(),
                                    isActive = false,
                                    tuningDirection = TuningDirection.IN_TUNE
                            )

            // Only update if state changed (to avoid unnecessary recompositions)
            if (currentState.isActive != isActive ||
                            (isActive && currentState.tuningDirection != tuningDirection)
            ) {

                updatedKnobsState[string] =
                        currentState.copy(
                                isActive = isActive,
                                tuningDirection =
                                        if (isActive) tuningDirection else TuningDirection.IN_TUNE
                        )
            }
        }

        _tunerKnobsState.value = updatedKnobsState
    }

    /** Start the tuner */
    fun startTuner() {
        tunerService.start()
    }

    /** Stop the tuner */
    fun stopTuner() {
        tunerService.stop()
    }

    /** Toggle the tuner on/off */
    fun toggleTuner() {
        Log.d("TunerViewModel", "toggleTuner()")
        if (_isListening.value) {
            stopTuner()
        } else {
            startTuner()
        }
    }

    /** Manually select a string to tune This could be used for a manual tuning mode */
    fun selectString(string: InstrumentString) {
        _activeString.value = string

        // Update knob states to show this string as active
        val updatedKnobsState = _tunerKnobsState.value.toMutableMap()

        InstrumentType.GUITAR_STANDARD.strings.forEach { s ->
            val currentState =
                updatedKnobsState[string]
                    ?: TunerState(
                        note = string.fullNoteName(),
                        isActive = false,
                        tuningDirection = TuningDirection.IN_TUNE
                    )

            updatedKnobsState[s] =
                    currentState.copy(
                            isActive = s == string,
                            tuningDirection =
                                    if (s == string) currentState.tuningDirection
                                    else TuningDirection.IN_TUNE
                    )
        }

        _tunerKnobsState.value = updatedKnobsState
    }

    /** Create the initial state for all tuner knobs */
    private fun createInitialKnobsState(): Map<InstrumentString, TunerState> {
        return InstrumentType.GUITAR_STANDARD.strings.associateWith { string ->
            TunerState(
                    note = string.fullNoteName(),
                    isActive = false,
                    tuningDirection = TuningDirection.IN_TUNE
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        tunerService.stop()
    }
}
