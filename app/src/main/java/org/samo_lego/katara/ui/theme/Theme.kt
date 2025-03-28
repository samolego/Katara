package org.samo_lego.katara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrownPale,
    secondary = BrownPaleGray,
    tertiary = BrownPale2
)

private val LightColorScheme = lightColorScheme(
    primary = Brown,
    secondary = BrownGray,
    tertiary = Brown2
)

val LocalTuneColors = staticCompositionLocalOf { lightTuneColors }

// Extension property to access tune colors from MaterialTheme
val MaterialTheme.tuneColors: TuneColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTuneColors.current

@Composable
fun KataraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val tuneColors = if (darkTheme) darkTuneColors else lightTuneColors

    CompositionLocalProvider(
        LocalTuneColors provides tuneColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}