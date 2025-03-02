package org.samo_lego.katara.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.samo_lego.katara.model.GuitarSpecification
import org.samo_lego.katara.util.InstrumentString
import org.samo_lego.katara.util.TuningDirection

class GuitarTunerViewModel : ViewModel() {
    // UI state
    private val _activeString = mutableStateOf<InstrumentString?>(null)
    val activeString: State<InstrumentString?> = _activeString

    private val _tuningDirection = mutableStateOf(TuningDirection.IN_TUNE)
    val tuningDirection: State<TuningDirection> = _tuningDirection

    private val _tuningValue = mutableFloatStateOf(0f)
    val tuningValue: State<Float> = _tuningValue

    // Guitar specifications
    val guitarSpec = GuitarSpecification.STANDARD_6_STRING

    // Actions
    fun toggleActiveString(string: InstrumentString) {
        _activeString.value = if (_activeString.value == string) null else string
    }

    fun updateTuningValue(newValue: Float) {
        _tuningValue.floatValue = newValue
        _tuningDirection.value = when {
            newValue > 0.2 -> TuningDirection.TOO_HIGH
            newValue < -0.2 -> TuningDirection.TOO_LOW
            else -> TuningDirection.IN_TUNE
        }
    }
}
