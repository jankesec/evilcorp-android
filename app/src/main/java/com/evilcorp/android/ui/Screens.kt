package com.evilcorp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.Area
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Difficulty

@Composable
fun HomeScreen(
    store: ProgressStore,
    modifier: Modifier = Modifier,
    onOpenChallenge: (String) -> Unit,
    onExploreAll: () -> Unit,
) {
    val all = ChallengeCatalog.all
    val solvedCount = store.solved.size
    val nextUnsolved = remember(store.solved.keys.toSet()) {
        all.firstOrNull { !store.isSolved(it.id) } ?: all.first()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (store.ctfMode) {
            item { CtfBanner(store) }
        }

        // 1. Clean .home-trust-row ledger + quick actions (no marketing paragraph clutter)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TrustLedgerRow(
                    items = listOf(
                        "${all.size}" to "Listed Labs",
                        "$solvedCount/${all.size}" to "Solved Records",
                        "${store.score()}" to "CTF Score",
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PrimaryButton(
                        text = "Explore ${all.size} Labs",
                        modifier = Modifier.weight(1f),
                        onClick = onExploreAll,
                    )
                    GhostButton(
                        text = if (store.ctfMode) "Stop CTF" else "Start CTF Mode",
                        modifier = Modifier.weight(1f),
                    ) {
                        if (store.ctfMode) store.stopCtf() else store.startCtf()
                    }
                }
            }
        }

        // 2. .lp-code-window target card (clean layout, no cramped ASCII tags)
        item {
            LpCodeWindow(
                title = "target — ${nextUnsolved.cwe.lowercase()}",
                actionText = "Launch ↗",
                onClick = { onOpenChallenge(nextUnsolved.id) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = nextUnsolved.title,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Text(
                            text = "${nextUnsolved.cwe} · ${nextUnsolved.masvs.removePrefix("MASVS-")} · ${nextUnsolved.maswe}",
                            fontSize = 11.sp,
                            color = cvssSeverityColor(nextUnsolved.cvss),
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    CveScoreBadge(nextUnsolved.cvss)
                }
            }
        }

        // 3. Attack Surface (.home-project-card 2-column grid from jankesec.com)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Kicker("ATTACK SURFACE")
                    Text(
                        text = "All ${all.size} →",
                        color = Muted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable(onClick = onExploreAll),
                    )
                }

                val areas = Area.entries
                for (row in areas.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (area in row) {
                            val areaIdx = areas.indexOf(area) + 1
                            val inArea = all.filter { it.area == area }
                            val doneInArea = inArea.count { store.isSolved(it.id) }
                            val pct = if (inArea.isEmpty()) 0f else doneInArea.toFloat() / inArea.size
                            val shape = RoundedCornerShape(8.dp)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .background(Panel)
                                    .border(1.dp, CardBorder, shape)
                                    .clickable { onExploreAll() }
                                    .padding(13.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Mono100),
                                        )
                                        Text(
                                            text = area.masvsCategory.removePrefix("MASVS-"),
                                            fontSize = 9.5.sp,
                                            color = Mono100,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 0.7.sp,
                                        )
                                    }
                                    Text(
                                        text = "%02d".format(areaIdx),
                                        fontSize = 10.sp,
                                        color = Muted,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                                Text(
                                    text = area.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(LineSubtle),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pct.coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .background(Mono100),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "$doneInArea/${inArea.size} solved",
                                        fontSize = 10.5.sp,
                                        color = Muted,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                    Text(
                                        text = "↗",
                                        fontSize = 12.sp,
                                        color = Mono100,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                            }
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Target Ledger (.home-cve-ledger from jankesec.com)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Kicker("TARGET LEDGER")
                    Text(
                        text = "View all →",
                        color = Muted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable(onClick = onExploreAll),
                    )
                }

                val previewList = all.take(6)
                val shape = RoundedCornerShape(8.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Panel)
                        .border(1.dp, CardBorder, shape),
                ) {
                    previewList.forEachIndexed { idx, c ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(LineSubtle),
                            )
                        }
                        JankesecLedgerRow(
                            index = idx + 1,
                            c = c,
                            solved = store.isSolved(c.id),
                            bookmarked = store.isBookmarked(c.id),
                            onClick = { onOpenChallenge(c.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean .home-cve-row implementation from https://jankesec.com:
 * [01]  [CWE-89 · CODE-4 · MASWE-0050 (colored) / Full Title (wraps up to 2 lines)]  [CVSS circle]  [↗]
 */
@Composable
fun JankesecLedgerRow(
    index: Int,
    c: Challenge,
    solved: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit,
) {
    val sevColor = cvssSeverityColor(c.cvss)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "%02d".format(index),
            color = Muted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(22.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "${c.cwe} · ${c.masvs.removePrefix("MASVS-")} · ${c.maswe}",
                    color = sevColor,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (bookmarked) {
                    EvilIcon(IconKind.Bookmark, tint = Mono100, size = 11.dp)
                }
            }
            Text(
                text = c.title,
                color = TextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp,
            )
        }
        if (solved) {
            BadgeView("SOLVED", Green)
        } else {
            CveScoreBadge(c.cvss)
        }
        Text(
            text = "↗",
            color = Mono100,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
fun LabsScreen(
    store: ProgressStore,
    modifier: Modifier = Modifier,
    onOpenChallenge: (String) -> Unit,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var diffFilter by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var savedOnly by rememberSaveable { mutableStateOf(false) }

    val filtered = ChallengeCatalog.all.filter { c ->
        (diffFilter == null || c.difficulty == diffFilter) &&
            (!savedOnly || store.isBookmarked(c.id)) &&
            (
                search.isBlank() ||
                    c.title.contains(search, ignoreCase = true) ||
                    c.cwe.contains(search, ignoreCase = true) ||
                    c.masvs.contains(search, ignoreCase = true) ||
                    c.maswe.contains(search, ignoreCase = true)
                )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 17.dp) },
                placeholder = {
                    Text(
                        "Search CWE-89, STORAGE-1, SQLite...",
                        color = Muted,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel,
                    focusedBorderColor = Mono100,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )
        }

        // Single clean filter row (no clipped second CWE row)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(
                    text = "All (${ChallengeCatalog.all.size})",
                    selected = diffFilter == null && !savedOnly,
                    onClick = {
                        diffFilter = null
                        savedOnly = false
                    },
                )
                Difficulty.entries.forEach { d ->
                    FilterPill(
                        text = d.label,
                        selected = diffFilter == d,
                        onClick = { diffFilter = if (diffFilter == d) null else d },
                    )
                }
                FilterPill(
                    text = "Saved (${store.bookmarks.size})",
                    selected = savedOnly,
                    onClick = { savedOnly = !savedOnly },
                )
            }
        }

        item {
            val shape = RoundedCornerShape(8.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Panel)
                    .border(1.dp, CardBorder, shape),
            ) {
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No records matching current filter.",
                            color = Muted,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                } else {
                    filtered.forEachIndexed { idx, c ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(LineSubtle),
                            )
                        }
                        JankesecLedgerRow(
                            index = idx + 1,
                            c = c,
                            solved = store.isSolved(c.id),
                            bookmarked = store.isBookmarked(c.id),
                            onClick = { onOpenChallenge(c.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MasvsScreen(
    store: ProgressStore,
    modifier: Modifier = Modifier,
    onOpenChallenge: (String) -> Unit,
) {
    val all = ChallengeCatalog.all
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker("COMPLIANCE MATRIX")
                Text(
                    "OWASP MASVS v2",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }
        itemsIndexed(Area.entries) { areaIdx, area ->
            val inArea = all.filter { it.area == area }
            val done = inArea.count { store.isSolved(it.id) }
            val pct = if (inArea.isEmpty()) 0f else done.toFloat() / inArea.size
            Card {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "%02d · %s".format(areaIdx + 1, area.masvsCategory),
                            fontSize = 10.sp,
                            color = Muted,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            area.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                        )
                    }
                    BadgeView("$done/${inArea.size} SOLVED", if (done == inArea.size && inArea.isNotEmpty()) Green else Mono100)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(LineSubtle),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Mono100),
                    )
                }
                inArea.forEachIndexed { idx, c ->
                    if (idx > 0) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))
                    }
                    val solved = store.isSolved(c.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChallenge(c.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (solved) {
                            EvilIcon(IconKind.Check, tint = Green, size = 15.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .border(1.2.dp, Muted.copy(alpha = 0.5f), CircleShape),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(c.title, fontSize = 13.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(
                                "${c.masvs} · ${c.maswe} · ${c.cwe}",
                                fontSize = 10.5.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Text("↗", color = Mono100, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(store: ProgressStore, modifier: Modifier = Modifier) {
    val all = ChallengeCatalog.all
    val totalPossible = all.sumOf { it.difficulty.points }
    val solvedCount = store.solved.size
    val totalHints = store.hints.values.sum()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker("OPERATOR TELEMETRY")
                Text(
                    "Assessment Ledger",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        item {
            TrustLedgerRow(
                items = listOf(
                    "${store.score()}/$totalPossible" to "Total Score",
                    "$solvedCount/${all.size}" to "Labs Solved",
                    "$totalHints" to "Hints Used",
                ),
            )
        }

        item {
            Card {
                Kicker("BY DIFFICULTY TIER")
                Difficulty.entries.forEach { d ->
                    val list = all.filter { it.difficulty == d }
                    val done = list.count { store.isSolved(it.id) }
                    val pct = if (list.isEmpty()) 0f else done.toFloat() / list.size
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${d.label.uppercase()} (${d.points} PTS)",
                            fontSize = 11.5.sp,
                            color = difficultyColor(d),
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            "$done / ${list.size}",
                            fontSize = 11.5.sp,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(LineSubtle),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(difficultyColor(d)),
                        )
                    }
                }
            }
        }

        item {
            Card {
                Kicker("BY MASVS DOMAIN")
                Area.entries.forEach { area ->
                    val list = all.filter { it.area == area }
                    val done = list.count { store.isSolved(it.id) }
                    val pct = if (list.isEmpty()) 0f else done.toFloat() / list.size
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${area.label} (${area.masvsCategory})",
                            fontSize = 12.sp,
                            color = TextPrimary,
                        )
                        Text(
                            "$done/${list.size}",
                            fontSize = 11.5.sp,
                            color = Muted,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(LineSubtle),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pct.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(Mono100),
                        )
                    }
                }
            }
        }
    }
}
