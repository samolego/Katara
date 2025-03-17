package org.samo_lego.katara.util

data class TunerState(
    val note: String,
    val rotation: Float = 0f,
    val isActive: Boolean = false,
    val tuningDirection: TuningDirection = TuningDirection.IN_TUNE
)

