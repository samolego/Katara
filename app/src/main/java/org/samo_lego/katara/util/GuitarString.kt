package org.samo_lego.katara.util

/**
 * Represents guitar strings in standard tuning. The strings are ordered from high to low by
 * frequency.
 */
enum class GuitarString(val stringNumber: Int, val noteName: String, val octave: Int) {
    E2(6, "E", 2), // Low E (6th string)
    A2(5, "A", 2),
    D3(4, "D", 3),
    G3(3, "G", 3),
    B3(2, "B", 3),
    E4(1, "E", 4); // High E (1st string)

    /** Returns the full note name including octave (e.g., "E4") */
    fun fullNoteName(): String = "$noteName$octave"

    companion object {
        /** Find a guitar string by string number (1-6) */
        fun fromStringNumber(number: Int): GuitarString? {
            return GuitarString.entries.find { it.stringNumber == number }
        }
    }
}
