package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.remember

private val DarkColorScheme = darkColorScheme(
    primary = CalmTealPrimary,
    secondary = CalmTealDark,
    tertiary = CalmTealLight,
    background = OnSurfaceText, // Slate charcoal background for dark theme
    surface = ColorBorderValueHolder.DarkSurface,
    error = ErrorRed,
    onPrimary = SoftSurface,
    onSecondary = SoftSurface,
    onBackground = SoftBackground,
    onSurface = SoftBackground
)

private val LightColorScheme = lightColorScheme(
    primary = CalmTealPrimary,
    secondary = CalmTealDark,
    tertiary = CalmTealLight,
    background = SoftBackground,
    surface = SoftSurface,
    error = ErrorRed,
    onPrimary = SoftSurface,
    onSecondary = SoftSurface,
    onBackground = OnSurfaceText,
    onSurface = OnSurfaceText
)

// Helper object to host static colors for cleanliness
object ColorBorderValueHolder {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF334155) // Slate 700
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSizeScale: Float = 1.0f,
    // Dynamic color is available on Android 12+ (disable for strict themed branding look if needed)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val scaledTypography = remember(fontSizeScale) {
        getScaledTypography(fontSizeScale)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides scaledTypography.bodyLarge.copy(fontFamily = CustomFontFamily)
        ) {
            content()
        }
    }
}
