package org.samo_lego.katara.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val BrownPale = Color(0xFFCBAF85)
val BrownPaleGray = Color(0xFFCCC2B9)
val BrownPale2 = Color(0xFFD9973A)

val Brown = Color(0xFF644C2D)
val BrownGray = Color(0xFF6E6159)
val Brown2 = Color(0xFF6C4D16)

val StringHighlight = Color(0xFFFF9800)

// Light theme tuning colors
val TuneTooLowDark = Color(0xFF1976D2)
val TuneOkDark = Color(0xFF388E3C)
val TuneTooHighDark = Color(0xFFEF6C00)

// Dark theme tuning colors
val TuneTooLowLight = Color(0xFF90CAF9)
val TuneOkLight = Color(0xFF81C784)
val TuneTooHighLight = Color(0xFFFFB74D)

// For backward compatibility
val TuneTooLow = TuneTooLowLight
val TuneOk = TuneOkLight
val TuneTooHigh = TuneTooHighLight

@Immutable data class TuneColors(val tooLow: Color, val ok: Color, val tooHigh: Color)

val lightTuneColors =
        TuneColors(tooLow = TuneTooLowLight, ok = TuneOkLight, tooHigh = TuneTooHighLight)

val darkTuneColors = TuneColors(tooLow = TuneTooLowDark, ok = TuneOkDark, tooHigh = TuneTooHighDark)
