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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
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
            .systemBarsPadding()
            .imePadding(),
    ) {
        // Top header bar in jankesec.com style
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xF0060606))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = prevId != null) { prevId?.let(onSelectChallenge) }
                        .padding(6.dp),
                ) {
                    EvilIcon(
                        IconKind.ChevronLeft,
                        tint = if (prevId != null) Mono100 else Muted.copy(alpha = 0.3f),
                        size = 18.dp,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "%02d // %s".format(idx + 1, c.title),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        BadgeView(c.cwe, cvssSeverityColor(c.cvss))
                        BadgeView(c.masvs, TextSoft)
                        BadgeView(c.maswe, TextSoft)
                        BadgeView("CVSS ${cvssNumericString(c.cvss)}", cvssSeverityColor(c.cvss))
                        BadgeView(c.difficulty.label.uppercase(), difficultyColor(c.difficulty))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = nextId != null) { nextId?.let(onSelectChallenge) }
                        .padding(6.dp),
                ) {
                    EvilIcon(
                        IconKind.ChevronRight,
                        tint = if (nextId != null) Mono100 else Muted.copy(alpha = 0.3f),
                        size = 18.dp,
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
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { store.toggleSolved(c) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = if (solvedPoints != null) "[✓] SOLVED (+$solvedPoints PTS)" else "[ ] MARK SOLVED",
                        color = if (solvedPoints != null) Green else Muted,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { store.toggleBookmark(c.id) }
                        .padding(6.dp),
                ) {
                    Text(
                        text = if (bookmarked) "★ SAVED" else "☆ SAVE",
                        color = if (bookmarked) Mono100 else Muted,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "ESC // LABS",
                        color = Mono100,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))

        // Segmented picker: Lab | Hints | Frida
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (section) {
                0 -> {
                    Card {
                        Kicker("${c.area.masvsCategory} // BRIEFING")
                        Text(c.description, fontSize = 13.5.sp, color = TextPrimary, lineHeight = 19.sp)
                        Text(
                            "Area: ${c.area.label} · ${c.masvs} · ${c.maswe} · CVSS: ${c.cvss}",
                            fontSize = 11.sp,
                            color = Muted,
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    LabRegistry.entries[c.id]?.content?.invoke()

                    Card {
                        Kicker("FLAG VERIFICATION")
                        if (solvedPoints != null) {
                            Text(
                                "[✓] SOLVED · +$solvedPoints PTS AWARDED",
                                color = Green,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                        } else {
                            OutlinedTextField(
                                value = flag,
                                onValueChange = { flag = it },
                                singleLine = true,
                                label = { Text("EVILCORP{…}", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BgSoft,
                                    unfocusedContainerColor = BgSoft,
                                    focusedBorderColor = Mono100,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                            PrimaryButton("Verify Flag") {
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
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Kicker("PROGRESSIVE DISCLOSURE")
                    Text(
                        "Each revealed hint reduces the challenge award by ${Scoring.HINT_PENALTY} pts (minimum 50%).",
                        fontSize = 12.sp,
                        color = Muted,
                    )
                    if (hintsUsed == 0) {
                        Card {
                            Text(
                                "No hints revealed yet. Inspect the target binary and runtime logs first.",
                                color = Muted,
                                fontSize = 12.5.sp,
                            )
                        }
                    }
                    c.hints.take(hintsUsed).forEachIndexed { i, h ->
                        Card {
                            BadgeView("HINT 0${i + 1}", Mono100)
                            Text(h, fontSize = 13.5.sp, color = TextPrimary, lineHeight = 19.sp)
                        }
                    }
                    if (hintsUsed < c.hints.size) {
                        PrimaryButton("Reveal Hint 0${hintsUsed + 1}") {
                            store.revealHint(c.id, c.hints.size)
                        }
                    }
                }
                2 -> {
                    Kicker("INSTRUMENTATION & ADB")
                    LpCodeWindow(
                        title = "${c.id} — exploit snippet",
                        actionText = if (copied) "COPIED" else "COPY",
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText(c.id, c.tooling))
                            copied = true
                        },
                    ) {
                        Text(
                            text = c.tooling,
                            color = TextSoft,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp,
                        )
                    }
                    Card {
                        Kicker("QUICK REFERENCE")
                        Text("01  adb shell / adb logcat -s EvilCorpAuth", fontSize = 11.5.sp, color = Muted, fontFamily = FontFamily.Monospace)
                        Text("02  frida -U -f com.evilcorp.android -l hook.js", fontSize = 11.5.sp, color = Muted, fontFamily = FontFamily.Monospace)
                        Text("03  jadx-gui app-debug.apk", fontSize = 11.5.sp, color = Muted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
