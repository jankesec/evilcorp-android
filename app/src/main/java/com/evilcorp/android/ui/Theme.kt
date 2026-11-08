package com.evilcorp.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Exact design tokens from https://jankesec.com (:root CSS variables)
val Bg = Color(0xFF040404)            // --bg: #040404
val BgSoft = Color(0xFF080808)        // --bg-soft: #080808
val Panel = Color(0xFF0B0B0B)         // --panel: #0b0b0b
val Elevated = Color(0xFF111111)      // --panel-2: #111111
val CodeBg = Color(0xFF070707)        // --code-bg: #070707
val CardBorder = Color(0x1CFFFFFF)    // --glass-border: #ffffff1c
val LineSubtle = Color(0x17FFFFFF)    // --line: #ffffff17
val LineStrong = Color(0x2EFFFFFF)    // --line-strong: #ffffff2e

// Typography & Monochrome scale
val TextPrimary = Color(0xFFF2F2F2)   // --text: #f2f2f2
val TextSoft = Color(0xFFD6D6D6)      // --text-soft: #d6d6d6
val Muted = Color(0xFFA3A3A3)         // --muted: #a3a3a3
val Mono100 = Color(0xFFF4F4F4)       // --mono-100 / --accent: #f4f4f4
val Mono200 = Color(0xFFE2E2E2)       // --mono-200: #e2e2e2
val Mono500 = Color(0xFF9A9A9A)       // --mono-500: #9a9a9a

// Severity colors from https://jankesec.com
val Red = Color(0xFFE8615C)           // --sev-critical: #e8615c
val Accent = Color(0xFFE2854B)        // --sev-high: #e2854b
val Amber = Color(0xFFE2854B)         // --sev-high: #e2854b
val Yellow = Color(0xFFC9A9D4)        // --sev-medium: #c9a9d4
val SevMedium = Color(0xFFC9A9D4)     // --sev-medium: #c9a9d4
val Cyan = Color(0xFF7FA8D4)          // --sev-low: #7fa8d4
val Green = Color(0xFF8BD49C)         // muted terminal green for solved state
val SevInfo = Color(0xFF8A8F99)       // --sev-info: #8a8f99

private val Scheme = darkColorScheme(
    primary = Mono100,
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
