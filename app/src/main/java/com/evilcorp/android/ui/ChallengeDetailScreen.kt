package com.evilcorp.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.labs.LabRegistry
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Scoring

@Composable
fun ChallengeDetailScreen(
    c: Challenge,
    store: ProgressStore,
    onBack: () -> Unit,
    onSelectChallenge: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val all = ChallengeCatalog.all
    val idx = all.indexOfFirst { it.id == c.id }.coerceAtLeast(0)
    val prevId = all.getOrNull(idx - 1)?.id
    val nextId = all.getOrNull(idx + 1)?.id

    val hintsUsed = store.hintsUsed(c.id)
    val solvedPoints = store.solved[c.id]
    val bookmarked = store.isBookmarked(c.id)

    var section by rememberSaveable(c.id) { mutableIntStateOf(0) }
    var flag by rememberSaveable(c.id) { mutableStateOf("") }
    var feedback by rememberSaveable(c.id) { mutableStateOf("") }
    var copied by remember(c.id) { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // Top header bar with Prev / Title + Taxonomy Badges / Next
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Panel)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = prevId != null) { prevId?.let(onSelectChallenge) }
                        .padding(6.dp),
                ) {
                    EvilIcon(
                        IconKind.ChevronLeft,
                        tint = if (prevId != null) Color.White else Muted.copy(alpha = 0.3f),
                        size = 20.dp,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = c.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        BadgeView(c.cwe, Color.White)
                        BadgeView(c.masvs, Color.White)
                        BadgeView(c.maswe, Cyan)
                        BadgeView(c.cvss, Yellow)
                        BadgeView(c.difficulty.label, difficultyColor(c.difficulty))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = nextId != null) { nextId?.let(onSelectChallenge) }
                        .padding(6.dp),
                ) {
                    EvilIcon(
                        IconKind.ChevronRight,
                        tint = if (nextId != null) Color.White else Muted.copy(alpha = 0.3f),
                        size = 20.dp,
                    )
                }
            }

            // Action strip: Mark Solved | Bookmark | Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { store.toggleSolved(c) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    EvilIcon(
                        IconKind.Check,
                        tint = if (solvedPoints != null) Green else Muted,
                        size = 16.dp,
                    )
                    Text(
                        text = if (solvedPoints != null) "Solved (+$solvedPoints pts)" else "Mark Solved",
                        color = if (solvedPoints != null) Green else Muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { store.toggleBookmark(c.id) }
                        .padding(6.dp),
                ) {
                    EvilIcon(
                        IconKind.Bookmark,
                        tint = if (bookmarked) Accent else Muted,
                        size = 17.dp,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    EvilIcon(IconKind.Close, tint = Muted, size = 14.dp)
                    Text("Labs", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Segmented picker: Lab | Hints | Frida
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SegmentedControl(
                tabs = listOf("Lab", "Hints ($hintsUsed/${c.hints.size})", "Frida & Tools"),
                selectedIndex = section,
                onSelect = { section = it },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (section) {
                0 -> {
                    Card {
                        Text(c.description, fontSize = 14.sp, color = Color.White)
                        Text(
                            "Area: ${c.area.label} · Difficulty: ${c.difficulty.label} (${c.difficulty.points} pts) · CVSS: ${c.cvss}",
                            fontSize = 12.sp,
                            color = Muted,
                        )
                    }

                    LabRegistry.entries[c.id]?.content?.invoke()

                    Card {
                        SectionTitle("Submit Flag")
                        if (solvedPoints != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                EvilIcon(IconKind.Check, tint = Green, size = 18.dp)
                                Text(
                                    "Solved · +$solvedPoints pts",
                                    color = Green,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = flag,
                                onValueChange = { flag = it },
                                singleLine = true,
                                label = { Text("EVILCORP{…}") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            PrimaryButton("Submit") {
                                feedback = if (LabRegistry.isCorrect(c.id, flag)) {
                                    store.markSolved(c)
                                    "Correct!"
                                } else {
                                    "Not quite. Flags look like EVILCORP{...}."
                                }
                            }
                            if (feedback.isNotEmpty()) {
                                Text(
                                    feedback,
                                    color = if (feedback == "Correct!") Green else Red,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Text("Hints", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Each revealed hint reduces the challenge award by ${Scoring.HINT_PENALTY} pts (minimum 50%).",
                        fontSize = 12.sp,
                        color = Muted,
                    )
                    if (hintsUsed == 0) {
                        Card {
                            Text("No hints revealed yet. Try inspecting the app first!", color = Muted, fontSize = 13.sp)
                        }
                    }
                    c.hints.take(hintsUsed).forEachIndexed { i, h ->
                        Card {
                            BadgeView("Hint ${i + 1}", Accent)
                            Text(h, fontSize = 14.sp, color = Color.White)
                        }
                    }
                    if (hintsUsed < c.hints.size) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Yellow.copy(alpha = 0.15f))
                                .clickable { store.revealHint(c.id, c.hints.size) }
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            EvilIcon(IconKind.Lightbulb, tint = Yellow, size = 18.dp)
                            Text(
                                "Reveal Hint ${hintsUsed + 1}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                2 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Frida & ADB Tooling", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Run on your workstation against the connected emulator/device:", fontSize = 12.sp, color = Accent)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Elevated)
                                .clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText(c.id, c.tooling))
                                    copied = true
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                        ) {
                            Text(
                                if (copied) "Copied" else "Copy",
                                color = if (copied) Green else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Mono(c.tooling, color = Accent)
                    Card {
                        SectionTitle("Quick Reference")
                        Text("· Connect emulator or test device via ADB", fontSize = 13.sp, color = Muted)
                        Text("· Frida: frida -U -f com.evilcorp.android -l hook.js", fontSize = 13.sp, color = Muted)
                        Text("· Decompile APK: jadx-gui app-debug.apk", fontSize = 13.sp, color = Muted)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
