package com.john.inkwell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.john.inkwell.data.FontPairing

private val InkwellColorScheme = lightColorScheme(
    background = PaperCream,
    surface = PaperCream,
    surfaceVariant = PaperCreamDark,
    onBackground = InkBrown,
    onSurface = InkBrown,
    primary = AccentSienna,
    onPrimary = PaperCream,
    secondary = InkFaded
)

@Composable
fun InkwellTheme(
    accent: Color = AccentSienna,
    fontPairing: FontPairing = FontPairing.CLASSIC_SERIF,
    content: @Composable () -> Unit
) {
    val scheme = InkwellColorScheme.copy(primary = accent)
    MaterialTheme(
        colorScheme = scheme,
        typography = typographyFor(fontPairing),
        content = content
    )
}
