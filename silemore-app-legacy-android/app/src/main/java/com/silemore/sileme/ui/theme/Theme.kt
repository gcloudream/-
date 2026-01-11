package com.silemore.sileme.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = CoralWarm,
    secondary = InkSoft,
    background = WarmBackground,
    surface = Mist,
    surfaceVariant = Fog,
    onPrimary = Color.White,
    onSecondary = Paper,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    error = Color(0xFFD32F2F),
    onError = Color.White,
    outline = CoralWarm.copy(alpha = 0.3f),
    outlineVariant = Fog
)

private val DarkColors = darkColorScheme(
    primary = MintFresh,
    secondary = DarkInk,
    background = WarmBackgroundDark,
    surface = DarkMist,
    surfaceVariant = DarkFog,
    onPrimary = DarkPaper,
    onSecondary = DarkPaper,
    onBackground = DarkInk,
    onSurface = DarkInk,
    onSurfaceVariant = DarkInkSoft,
    error = Color(0xFFFF6B6B),
    onError = DarkPaper,
    outline = MintFresh.copy(alpha = 0.3f),
    outlineVariant = DarkFog
)

private val SilemoreShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
)

@Composable
fun SilemoreTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SilemoreTypography,
        shapes = SilemoreShapes,
        content = content
    )
}
