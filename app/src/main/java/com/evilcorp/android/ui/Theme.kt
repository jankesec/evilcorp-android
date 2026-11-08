package com.evilcorp.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Refined dark monochrome palette inspired by jankesec.com & native Android dark UI
val Bg = Color(0xFF060709)
val BgSoft = Color(0xFF0A0C10)
val Panel = Color(0xFF101217)
val Elevated = Color(0xFF171A21)
val CodeBg = Color(0xFF090B0E)
val CardBorder = Color(0xFF222630)
val LineSubtle = Color(0xFF1A1D24)
val LineStrong = Color(0xFF2C313D)

val TextPrimary = Color(0xFFF3F4F6)
val TextSoft = Color(0xFFD1D5DB)
val Muted = Color(0xFF8B92A5)
val Mono100 = Color(0xFFF3F4F6)
val Mono200 = Color(0xFFE5E7EB)
val Mono500 = Color(0xFF9CA3AF)

// Semantic & Severity Accents
val Green = Color(0xFF10B981)
val Amber = Color(0xFFF59E0B)
val Yellow = Color(0xFFF59E0B)
val Red = Color(0xFFEF4444)
val Accent = Color(0xFFF59E0B)
val Cyan = Color(0xFF38BDF8)
val SevMedium = Color(0xFFF59E0B)
val SevInfo = Color(0xFF9CA3AF)

private val Scheme = darkColorScheme(
    primary = TextPrimary,
    secondary = TextSoft,
    tertiary = Cyan,
    background = Bg,
    surface = Panel,
    surfaceVariant = Elevated,
    onPrimary = Bg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun EvilCorpTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
