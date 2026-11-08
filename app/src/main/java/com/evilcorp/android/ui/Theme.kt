package com.evilcorp.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF08080C)
val Panel = Color(0xFF121218)
val Elevated = Color(0xFF1A1A22)
val CardBorder = Color(0x14FFFFFF)
val Accent = Color(0xFFF5A623)
val Green = Color(0xFF42D77D)
val Amber = Color(0xFFF5A623)
val Red = Color(0xFFFF4D4F)
val Cyan = Color(0xFF38BDF8)
val Yellow = Color(0xFFFACC15)
val Blue = Color(0xFF3B82F6)
val Muted = Color(0xFF8E8E98)

private val Scheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Accent,
    onSecondary = Color.Black,
    background = Bg,
    onBackground = Color.White,
    surface = Panel,
    onSurface = Color.White,
    surfaceVariant = Elevated,
    onSurfaceVariant = Muted,
    outline = Color(0xFF2E2E3A),
)

@Composable
fun EvilCorpTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
