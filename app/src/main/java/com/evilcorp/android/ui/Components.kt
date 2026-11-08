package com.evilcorp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Difficulty
import kotlinx.coroutines.delay

fun difficultyColor(d: Difficulty): Color = when (d) {
    Difficulty.Easy -> Cyan        // --sev-low: #7fa8d4
    Difficulty.Medium -> SevMedium // --sev-medium: #c9a9d4
    Difficulty.Hard -> Red         // --sev-critical: #e8615c
}

fun cvssNumericString(cvss: String): String =
    cvss.trim().substringBefore(" ").trim()

fun cvssSeverityColor(cvss: String): Color {
    val score = cvssNumericString(cvss).toFloatOrNull() ?: 5.0f
    return when {
        score >= 8.5f -> Red       // --sev-critical: #e8615c
        score >= 7.0f -> Accent    // --sev-high: #e2854b
        score >= 4.0f -> SevMedium // --sev-medium: #c9a9d4
        else -> Cyan               // --sev-low: #7fa8d4
    }
}

/**
 * Monochrome obsidian backdrop from https://jankesec.com:
 * #040404 + subtle ctos-grid horizontal lines + soft top vignette.
 */
@Composable
fun LiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val pitch = 30.dp.toPx()
            var y = 0f
            while (y < h) {
                drawLine(
                    color = Color(0x07FFFFFF),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f,
                )
                y += pitch
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x10FFFFFF), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.12f),
                    radius = w * 0.8f,
                ),
                center = Offset(w * 0.5f, h * 0.12f),
                radius = w * 0.8f,
            )
        }
        content()
    }
}

fun Modifier.liquidGlass(
    cornerRadius: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    tint: Color = Mono100,
    baseAlpha: Float = 0.05f,
    rimAlpha: Float = 0.12f,
    tintStrength: Float = baseAlpha,
): Modifier = this
    .clip(shape)
    .background(Panel, shape)
    .border(1.dp, CardBorder, shape)

@Composable
fun GlassOrb(
    tint: Color = Mono100,
    size: Dp = 38.dp,
    cornerRadius: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(Elevated)
            .border(1.dp, CardBorder, shape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * Compact single-bar top chrome in https://jankesec.com style:
 * `evilcorp_` brand on left, section indicator + score pill on right.
 */
@Composable
fun JankesecHeader(
    section: String,
    score: Int,
    solvedCount: Int,
    totalCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF2040404)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "evilcorp",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                )
                Text(
                    text = "_",
                    color = Muted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(Muted))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = section.uppercase(),
                    color = Muted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.4.sp,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Elevated)
                        .border(1.dp, LineSubtle, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "$solvedCount/$totalCount",
                        color = TextSoft,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Mono100)
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "$score PTS",
                        color = Bg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))
    }
}

/** .kicker from https://jankesec.com: `── LABEL` in tracked uppercase monospace. */
@Composable
fun Kicker(text: String, color: Color = Mono100) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(1.dp)
                .background(color),
        )
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
        )
    }
}

/** .lp-code-window from https://jankesec.com with 3 monochrome window dots and terminal body. */
@Composable
fun LpCodeWindow(
    title: String,
    actionText: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CodeBg)
            .border(1.dp, CardBorder, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Elevated)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Mono200))
            Spacer(Modifier.width(5.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(Mono500))
            Spacer(Modifier.width(5.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF4D4D4D)))
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                color = Muted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (actionText.isNotEmpty()) {
                Text(
                    text = actionText.uppercase(),
                    color = Mono100,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

/** Circular CVSS score badge matching .home-cve-row .cve-score on https://jankesec.com. */
@Composable
fun CveScoreBadge(cvss: String) {
    val color = cvssSeverityColor(cvss)
    val shortScore = cvssNumericString(cvss)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = shortScore,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** .home-trust-row from https://jankesec.com: 3-column bordered stat ledger with 1px dividers. */
@Composable
fun TrustLedgerRow(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel)
            .border(1.dp, LineStrong, shape)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { idx, (value, label) ->
            if (idx > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(LineSubtle),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )
                Text(
                    text = label.uppercase(),
                    color = Muted,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.7.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Panel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun BadgeView(text: String, color: Color = TextSoft) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.11f))
            .border(1.dp, color.copy(alpha = 0.30f), shape)
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun Chip(text: String, color: Color = TextSoft) {
    BadgeView(text = text, color = color)
}

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    color: Color = Mono100,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    val bg = if (selected) Mono100 else Panel
    val border = if (selected) Mono100 else LineStrong
    val fg = if (selected) Bg else TextSoft
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Kicker(text = text)
}

@Composable
fun Mono(text: String, modifier: Modifier = Modifier, color: Color = TextSoft) {
    val shape = RoundedCornerShape(6.dp)
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CodeBg)
                .border(1.dp, LineSubtle, shape)
                .padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            lineHeight = 17.sp,
            color = color,
        )
    }
}

@Composable
fun Warning(text: String) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Red.copy(alpha = 0.10f))
            .border(1.dp, Red.copy(alpha = 0.35f), shape)
            .padding(12.dp),
    ) {
        Text(text, color = Red, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

/** .button-accent from https://jankesec.com: stark #F4F4F4 fill, #040404 text, 6dp radius. */
@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Mono100)
            .border(1.dp, Mono100, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Bg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun AccentButton(
    text: String,
    icon: IconKind? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Mono100)
            .border(1.dp, Mono100, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            EvilIcon(icon, tint = Bg, size = 14.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = Bg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** .button-ghost from https://jankesec.com: dark panel fill, 1px border, #F2F2F2 text. */
@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Panel)
            .border(1.dp, LineStrong, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun StatCardView(
    value: String,
    label: String,
    icon: IconKind? = null,
    iconColor: Color = Mono100,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
            )
            Text(
                text = label.uppercase(),
                fontSize = 9.5.sp,
                color = Muted,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.7.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun StatRow(vararg items: Pair<String, String>) {
    TrustLedgerRow(items = items.map { (label, value) -> value to label })
}

@Composable
fun CtfBanner(store: ProgressStore) {
    var elapsedSec by remember { mutableLongStateOf(0L) }
    var minimized by remember { mutableStateOf(false) }

    LaunchedEffect(store.ctfMode, store.ctfStartMs) {
        while (store.ctfMode && store.ctfStartMs > 0L) {
            elapsedSec = ((System.currentTimeMillis() - store.ctfStartMs) / 1000L).coerceAtLeast(0L)
            delay(1000L)
        }
    }

    val mins = elapsedSec / 60
    val secs = elapsedSec % 60
    val shape = RoundedCornerShape(8.dp)

    if (minimized) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .clip(shape)
                    .background(Panel)
                    .border(1.dp, LineStrong, shape)
                    .clickable { minimized = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Red))
                Text(
                    "CTF · ${mins}m ${secs}s · ${store.score()} PTS",
                    color = Mono100,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = shape,
            border = BorderStroke(1.dp, LineStrong),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { minimized = true },
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Red))
                    Text(
                        "CTF SESSION",
                        color = Mono100,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Text(
                    "${mins}m ${secs}s · ${store.score()} PTS · ${store.solved.size}/${ChallengeCatalog.all.size}",
                    color = TextSoft,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Red.copy(alpha = 0.14f))
                        .border(1.dp, Red.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .clickable { store.stopCtf() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("END", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
