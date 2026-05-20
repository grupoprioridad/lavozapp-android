package com.lavozapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LvpRed,
    onPrimary = White,
    primaryContainer = LvpRed,
    secondary = LvpBlue,
    background = White,
    surface = LvpSurface,
    onBackground = LvpDark,
    onSurface = LvpDark,
    outline = LvpGray200
)

@Composable
fun LavozTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColorScheme, typography = Typography, content = content)
}
