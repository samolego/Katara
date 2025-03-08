package org.samo_lego.katara.util

enum class Note(val noteName: String) {
    A("A"),
    A_SHARP("A#"),
    B("B"),
    C("C"),
    C_SHARP("C#"),
    D("D"),
    D_SHARP("D#"),
    E("E"),
    F("F"),
    F_SHARP("F#"),
    G("G"),
    G_SHARP("G#")
}

data class InstrumentString(val stringNumber: Int, val noteFreq: NoteFrequency) {
    fun fullNoteName(): String = "${noteFreq.name}$"
}

enum class InstrumentType(val strings: List<InstrumentString>) {
    GUITAR_STANDARD(
            listOf(
                    InstrumentString(1, NoteFrequency.E4),
                    InstrumentString(2, NoteFrequency.B3),
                    InstrumentString(3, NoteFrequency.G3),
                    InstrumentString(4, NoteFrequency.D3),
                    InstrumentString(5, NoteFrequency.A2),
                    InstrumentString(6, NoteFrequency.E2),
            )
    ),
}

enum class NoteFrequency(note: Note, frequency: Double) {
    E4(Note.E, 329.63),
    B3(Note.B, 246.94),
    G3(Note.G, 196.0),
    D3(Note.D, 146.83),
    A2(Note.A, 110.0),
    E2(Note.E, 82.41),
}
