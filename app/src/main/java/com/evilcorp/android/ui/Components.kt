package com.evilcorp.android.ui

import androidx.compose.foundation.BorderStroke
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
    Difficulty.Easy -> Green
    Difficulty.Medium -> Amber
    Difficulty.Hard -> Red
}

fun cvssNumericString(cvss: String): String =
    cvss.trim().substringBefore(" ").trim()

fun cvssSeverityColor(cvss: String): Color {
    val score = cvssNumericString(cvss).toFloatOrNull() ?: 5.0f
    return when {
        score >= 8.5f -> Red
        score >= 7.0f -> Amber
        score >= 4.0f -> Cyan
        else -> Green
    }
}

@Composable
fun LiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(Bg), content = content)
}

fun Modifier.liquidGlass(
    cornerRadius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    tint: Color = TextPrimary,
    baseAlpha: Float = 0.05f,
    rimAlpha: Float = 0.12f,
    tintStrength: Float = baseAlpha,
): Modifier = this
    .clip(shape)
    .background(Panel, shape)
    .border(1.dp, CardBorder, shape)

@Composable
fun GlassOrb(
    tint: Color = TextPrimary,
    size: Dp = 40.dp,
    cornerRadius: Dp = 10.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.24f), shape),
        contentAlignment = Alignment.Center,
        content = content,
    )
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
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun BadgeView(text: String, color: Color = TextSoft) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.28f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
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
    color: Color = TextPrimary,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val bg = if (selected) TextPrimary else Panel
    val border = if (selected) TextPrimary else CardBorder
    val fg = if (selected) Bg else TextSoft
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = Muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
    )
}

@Composable
fun Mono(text: String, modifier: Modifier = Modifier, color: Color = TextSoft) {
    val shape = RoundedCornerShape(8.dp)
    SelectionContainer {
        Text(
            text = text,
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(CodeBg)
                .border(1.dp, CardBorder, shape)
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
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Red.copy(alpha = 0.10f))
            .border(1.dp, Red.copy(alpha = 0.30f), shape)
            .padding(12.dp),
    ) {
        Text(text, color = Red, fontSize = 12.5.sp, lineHeight = 18.sp)
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(TextPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Bg,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
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
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(TextPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Elevated)
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 12.5.sp,
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
    iconColor: Color = TextPrimary,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(10.dp),
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
                maxLines = 1,
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Muted,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun StatRow(vararg items: Pair<String, String>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (label, value) ->
            StatCardView(value = value, label = label, modifier = Modifier.weight(1f))
        }
    }
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
    val shape = RoundedCornerShape(10.dp)

    if (minimized) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .clip(shape)
                    .background(Panel)
                    .border(1.dp, CardBorder, shape)
                    .clickable { minimized = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Green))
                Text(
                    "CTF Active · ${mins}m ${secs}s · ${store.score()} pts",
                    color = TextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Panel,
            shape = shape,
            border = BorderStroke(1.dp, CardBorder),
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
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Green))
                    Text(
                        "CTF Session",
                        color = TextPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    "${mins}m ${secs}s · ${store.score()} pts · ${store.solved.size}/${ChallengeCatalog.all.size}",
                    color = TextSoft,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Red.copy(alpha = 0.14f))
                        .border(1.dp, Red.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .clickable { store.stopCtf() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("End", color = Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
