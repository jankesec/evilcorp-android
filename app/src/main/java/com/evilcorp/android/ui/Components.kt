package com.evilcorp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun Card(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Panel,
        contentColor = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun BadgeView(text: String, color: Color = Color.White) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
fun Chip(text: String, color: Color = Color.White) {
    BadgeView(text = text, color = color)
}

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    color: Color = Color.White,
    onClick: () -> Unit,
) {
    val bg = if (selected) color else Color.White.copy(alpha = 0.08f)
    val fg = if (selected) Color.Black else Color.White
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TagPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color.White else Color.White.copy(alpha = 0.07f)
    val fg = if (selected) Color.Black else Color(0xFFD4D4DC)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = Muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
    )
}

@Composable
fun Mono(text: String, modifier: Modifier = Modifier, color: Color = Color(0xFFE2E2EA)) {
    SelectionContainer {
        Text(
            text,
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFF060609), RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                .padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
            color = color,
        )
    }
}

@Composable
fun Warning(text: String) {
    Surface(
        color = Red.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Red.copy(alpha = 0.40f)),
    ) {
        Text(text, color = Color(0xFFFFB4B6), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun AccentButton(
    text: String,
    icon: IconKind? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.White),
    ) {
        if (icon != null) {
            EvilIcon(icon, tint = Color.White, size = 15.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.4.sp)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
    ) {
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun StatCardView(
    value: String,
    label: String,
    icon: IconKind? = null,
    iconColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 11.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (icon != null) {
                EvilIcon(icon, tint = iconColor, size = 15.dp)
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Muted)
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

/** Top posture strip matching EvilCorp iOS SecurityStatusBar. */
@Composable
fun SecurityStatusBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Panel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(IconKind.Unlock, "CLEARTEXT", Red)
            Spacer(Modifier.width(12.dp))
            StatusDot(IconKind.Network, "HTTP OK", Accent)
            Spacer(Modifier.width(12.dp))
            StatusDot(IconKind.Shield, "DBG/ROOT", Red)
            Spacer(Modifier.width(12.dp))
            StatusDot(IconKind.Folder, "BACKUP ON", Accent)
            Spacer(Modifier.width(12.dp))
            StatusDot(IconKind.Code, "evilcorp://", Cyan)
        }
    }
}

@Composable
private fun StatusDot(icon: IconKind, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EvilIcon(icon, tint = color, size = 10.dp)
        Text(
            label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

/** Live floating CTF mode banner matching EvilCorp iOS CTFBannerView. */
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

    if (minimized) {
        Box(Modifier.fillMaxWidth().padding(top = 6.dp), contentAlignment = Alignment.Center) {
            Surface(
                color = Panel,
                shape = CircleShape,
                border = BorderStroke(1.dp, Accent.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { minimized = false },
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EvilIcon(IconKind.Flag, tint = Accent, size = 12.dp)
                    Text(
                        "CTF · ${mins}m ${secs}s · ${store.score()} pts",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = Panel,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Accent.copy(alpha = 0.35f)),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { minimized = true },
                ) {
                    EvilIcon(IconKind.Flag, tint = Accent, size = 14.dp)
                    Text("CTF MODE", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CtfMetric("${mins}m ${secs}s", "elapsed", Color.White)
                    CtfMetric("${store.score()}", "pts", Accent)
                    CtfMetric("${store.solved.size}/${ChallengeCatalog.all.size}", "solved", Green)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Red.copy(alpha = 0.16f))
                        .clickable { store.stopCtf() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("END", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CtfMetric(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
        Text(label, color = Muted, fontSize = 8.sp)
    }
}
