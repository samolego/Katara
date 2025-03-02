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

data class InstrumentString(val stringNumber: Int, val note: Note, val octave: Int) {
    fun fullNoteName(): String = "${note.noteName}$octave"
}

enum class InstrumentType(val strings: List<InstrumentString>) {
    GUITAR_STANDARD(
            listOf(
                    InstrumentString(1, Note.E, 4),
                    InstrumentString(2, Note.B, 3),
                    InstrumentString(3, Note.G, 3),
                    InstrumentString(4, Note.D, 3),
                    InstrumentString(5, Note.A, 2),
                    InstrumentString(6, Note.E, 2)
            )
    ),
    /*BASS_STANDARD(
            listOf(
                    InstrumentString(1, Note.G, 2),
                    InstrumentString(2, Note.D, 2),
                    InstrumentString(3, Note.A, 1),
                    InstrumentString(4, Note.E, 1)
            )
    ),
    UKULELE_STANDARD(
            listOf(
                    InstrumentString(1, Note.A, 4),
                    InstrumentString(2, Note.E, 4),
                    InstrumentString(3, Note.C, 4),
                    InstrumentString(4, Note.G, 4)
            )
    )*/;

    fun getStringByNumber(number: Int): InstrumentString? {
        return strings.find { it.stringNumber == number }
    }
}
