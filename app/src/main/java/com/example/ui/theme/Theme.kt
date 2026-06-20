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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = SoftSurface,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = CalmTealDark,
    tertiary = CalmTealLight,
    background = DarkBackground,
    surface = DarkSurface,
    error = ErrorRed,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF1E293B), // Slate 800
    onSurfaceVariant = InactiveGrey,
    outline = CardBorderColorDark
)

private val LightColorScheme = lightColorScheme(
    primary = CalmTealPrimary,
    onPrimary = SoftSurface,
    primaryContainer = CalmTealLight,
    onPrimaryContainer = CalmTealDark,
    secondary = CalmTealDark,
    tertiary = CalmTealLight,
    background = SoftBackground,
    surface = SoftSurface,
    error = ErrorRed,
    onBackground = OnSurfaceText,
    onSurface = OnSurfaceText,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    outline = CardBorderColor
)

// Helper object to host static colors for cleanliness and compatibility
object ColorBorderValueHolder {
    val DarkSurface = Color(0xFF151F32) // matches DarkSurface
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSizeScale: Float = 1.0f,
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
