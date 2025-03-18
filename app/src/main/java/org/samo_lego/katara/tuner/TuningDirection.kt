package org.samo_lego.katara.tuner

/**
 * Direction of tuning
 */
enum class TuningDirection {
    TOO_LOW, // Need to tighten the string
    IN_TUNE, // String is correctly tuned
    TOO_HIGH // Need to loosen the string
}
