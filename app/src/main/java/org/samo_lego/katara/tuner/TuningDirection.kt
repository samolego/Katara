package org.samo_lego.katara.tuner

/**
 * Direction of tuning
 */
enum class TuningDirection {
    TOO_LOW, // Need to tighten the string (rotate knob from bottom to top)
    IN_TUNE, // String is correctly tuned (no rotation)
    TOO_HIGH // Need to loosen the string (rotate knob from top to bottom)
}
