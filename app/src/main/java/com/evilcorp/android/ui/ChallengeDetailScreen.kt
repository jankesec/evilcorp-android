package com.evilcorp.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        // Clean navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSoft)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EvilIcon(IconKind.ChevronLeft, tint = TextPrimary, size = 18.dp)
                Spacer(Modifier.width(4.dp))
                Text("Labs", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Text(
                text = c.title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = prevId != null) { prevId?.let(onSelectChallenge) }
                    .padding(6.dp),
            ) {
                EvilIcon(
                    IconKind.ChevronLeft,
                    tint = if (prevId != null) TextPrimary else Muted.copy(alpha = 0.3f),
                    size = 18.dp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = nextId != null) { nextId?.let(onSelectChallenge) }
                    .padding(6.dp),
            ) {
                EvilIcon(
                    IconKind.ChevronRight,
                    tint = if (nextId != null) TextPrimary else Muted.copy(alpha = 0.3f),
                    size = 18.dp,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { store.toggleBookmark(c.id) }
                    .padding(6.dp),
            ) {
                EvilIcon(
                    IconKind.Bookmark,
                    tint = if (bookmarked) Amber else Muted,
                    size = 18.dp,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(CardBorder))

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgeView(c.difficulty.label, difficultyColor(c.difficulty))
                                BadgeView("${c.difficulty.points} pts", TextSoft)
                                BadgeView("CVSS ${cvssNumericString(c.cvss)}", cvssSeverityColor(c.cvss))
                            }
                            if (solvedPoints != null) {
                                BadgeView("Solved (+$solvedPoints)", Green)
                            }
                        }
                        Text(
                            text = c.description,
                            fontSize = 13.5.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp,
                        )
                        Text(
                            text = "${c.cwe} · ${c.masvs} · ${c.maswe}",
                            fontSize = 11.5.sp,
                            color = Muted,
                            fontFamily = FontFamily.Monospace,
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
                                    "Challenge Solved · +$solvedPoints pts",
                                    color = Green,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = flag,
                                onValueChange = { flag = it },
                                singleLine = true,
                                label = { Text("EVILCORP{…}", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BgSoft,
                                    unfocusedContainerColor = BgSoft,
                                    focusedBorderColor = TextPrimary,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                ),
                            )
                            PrimaryButton("Submit Flag") {
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
                                    fontSize = 12.5.sp,
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Text("Progressive Hints", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "Each revealed hint reduces the challenge award by ${Scoring.HINT_PENALTY} pts (minimum 50%).",
                        fontSize = 12.5.sp,
                        color = Muted,
                    )
                    if (hintsUsed == 0) {
                        Card {
                            Text(
                                "No hints revealed yet. Inspect the target binary and runtime logs first.",
                                color = Muted,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    c.hints.take(hintsUsed).forEachIndexed { i, h ->
                        Card {
                            BadgeView("Hint ${i + 1}", Amber)
                            Text(h, fontSize = 13.5.sp, color = TextPrimary, lineHeight = 19.sp)
                        }
                    }
                    if (hintsUsed < c.hints.size) {
                        PrimaryButton("Reveal Hint ${hintsUsed + 1}") {
                            store.revealHint(c.id, c.hints.size)
                        }
                    }
                }
                2 -> {
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Frida & ADB Tooling", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Run against the connected device or emulator", fontSize = 12.sp, color = Muted)
                            }
                            GhostButton(if (copied) "Copied" else "Copy") {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText(c.id, c.tooling))
                                copied = true
                            }
                        }
                        Mono(c.tooling)
                    }
                    Card {
                        SectionTitle("Quick Reference")
                        Text("• adb logcat -s EvilCorpAuth", fontSize = 12.sp, color = Muted, fontFamily = FontFamily.Monospace)
                        Text("• frida -U -f com.evilcorp.android -l hook.js", fontSize = 12.sp, color = Muted, fontFamily = FontFamily.Monospace)
                        Text("• jadx-gui app-debug.apk", fontSize = 12.sp, color = Muted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
