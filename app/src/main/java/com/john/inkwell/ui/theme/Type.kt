package com.john.inkwell.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.john.inkwell.data.FontPairing

/**
 * Builds a Typography for the chosen [FontPairing]. Using the system
 * generic families (Serif / SansSerif / Monospace) rather than bundled
 * font files keeps the app lightweight — swap these for real font
 * resources later if a pairing needs a specific typeface.
 */
fun typographyFor(pairing: FontPairing): Typography {
    val (headerFamily, bodyFamily) = when (pairing) {
        FontPairing.CLASSIC_SERIF -> FontFamily.Serif to FontFamily.SansSerif
        FontPairing.MODERN_SANS -> FontFamily.SansSerif to FontFamily.SansSerif
        FontPairing.TYPEWRITER -> FontFamily.Monospace to FontFamily.Monospace
    }

    val timestampStyle = TextStyle(
        fontFamily = headerFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    )

    val bodyStyle = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp
    )

    return Typography(
        headlineSmall = TextStyle(
            fontFamily = headerFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        ),
        bodyLarge = bodyStyle,
        labelMedium = timestampStyle
    )
}
