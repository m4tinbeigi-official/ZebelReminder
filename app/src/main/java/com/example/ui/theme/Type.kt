package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ir.m4tinbeigi.taskreminder.R

// Centralized clean unified font family for modern readable RTL presentation loaded locally offline
val CustomFontFamily = FontFamily(
    Font(R.font.vazirmatn_light, weight = FontWeight.Light),
    Font(R.font.vazirmatn_regular, weight = FontWeight.Normal),
    Font(R.font.vazirmatn_medium, weight = FontWeight.Medium),
    Font(R.font.vazirmatn_medium, weight = FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, weight = FontWeight.Bold),
    Font(R.font.vazirmatn_bold, weight = FontWeight.ExtraBold),
    Font(R.font.vazirmatn_bold, weight = FontWeight.Black)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CustomFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)

private fun scaleTextStyle(style: TextStyle, scale: Float): TextStyle {
    val newSize = if (style.fontSize.isSp) (style.fontSize.value * scale).sp else style.fontSize
    val newHeight = if (style.lineHeight.isSp) (style.lineHeight.value * scale).sp else style.lineHeight
    return style.copy(fontSize = newSize, lineHeight = newHeight)
}

fun getScaledTypography(scale: Float): Typography {
    if (scale == 1.0f) return Typography
    return Typography(
        displayLarge = scaleTextStyle(Typography.displayLarge, scale),
        displayMedium = scaleTextStyle(Typography.displayMedium, scale),
        displaySmall = scaleTextStyle(Typography.displaySmall, scale),
        headlineLarge = scaleTextStyle(Typography.headlineLarge, scale),
        headlineMedium = scaleTextStyle(Typography.headlineMedium, scale),
        headlineSmall = scaleTextStyle(Typography.headlineSmall, scale),
        titleLarge = scaleTextStyle(Typography.titleLarge, scale),
        titleMedium = scaleTextStyle(Typography.titleMedium, scale),
        titleSmall = scaleTextStyle(Typography.titleSmall, scale),
        bodyLarge = scaleTextStyle(Typography.bodyLarge, scale),
        bodyMedium = scaleTextStyle(Typography.bodyMedium, scale),
        bodySmall = scaleTextStyle(Typography.bodySmall, scale),
        labelLarge = scaleTextStyle(Typography.labelLarge, scale),
        labelMedium = scaleTextStyle(Typography.labelMedium, scale),
        labelSmall = scaleTextStyle(Typography.labelSmall, scale)
    )
}
