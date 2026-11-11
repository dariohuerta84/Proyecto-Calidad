package com.trouvels360.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta tomada del frontend web (verde menta #8ed1b2, azul marino del banner, texto #0f1721)
val Mint = Color(0xFF8ED1B2)
val MintDark = Color(0xFF2F7D5E)
val Navy = Color(0xFF233463)
val Ink = Color(0xFF0F1721)
val Star = Color(0xFFF5B301)

private val Light = lightColorScheme(
    primary = MintDark,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Ink,
    secondary = Navy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE3F5),
    onSecondaryContainer = Navy,
    background = Color(0xFFF7FBF8),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEF2F1),
    onSurfaceVariant = Color(0xFF4A5553),
    outline = Color(0xFFC0C7C5),
)

private val Dark = darkColorScheme(
    primary = Mint,
    onPrimary = Ink,
    primaryContainer = MintDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB5C3EE),
    onSecondary = Navy,
    background = Ink,
    onBackground = Color(0xFFE6E9E8),
    surface = Color(0xFF17212C),
    onSurface = Color(0xFFE6E9E8),
    surfaceVariant = Color(0xFF243140),
    onSurfaceVariant = Color(0xFFC0C7C5),
)

@Composable
fun TrouvelsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
