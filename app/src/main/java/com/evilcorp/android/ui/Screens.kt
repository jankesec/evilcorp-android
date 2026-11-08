package com.evilcorp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.R
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
    val totalPossible = all.sumOf { it.difficulty.points }
    val pct = if (all.isEmpty()) 0f else solvedCount.toFloat() / all.size
    val nextUnsolved = remember(store.solved.keys.toSet()) {
        all.firstOrNull { !store.isSolved(it.id) } ?: all.first()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (store.ctfMode) {
            item { CtfBanner(store) }
        }

        // 1. Compact Header + 3-Metric Summary Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.evilcorp_logo),
                            contentDescription = "EvilCorp",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                        )
                        Column {
                            Text(
                                text = "EvilCorp",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Text(
                                text = "Android Security Lab · ${all.size} Targets",
                                fontSize = 12.sp,
                                color = Muted,
                            )
                        }
                    }
                    if (!store.ctfMode) {
                        GhostButton("Start CTF") { store.startCtf() }
                    } else {
                        GhostButton("Stop CTF") { store.stopCtf() }
                    }
                }

                // 3-Column Metrics + Progress Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Panel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MetricColumn(
                                value = "${store.score()}",
                                label = "Score / $totalPossible",
                                modifier = Modifier.weight(1f),
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(CardBorder),
                            )
                            MetricColumn(
                                value = "$solvedCount / ${all.size}",
                                label = "Labs Solved",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(CardBorder),
                            )
                            MetricColumn(
                                value = "${(pct * 100).toInt()}%",
                                label = "Completion",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Elevated),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(TextPrimary),
                            )
                        }
                    }
                }
            }
        }

        // 2. Continue / Next Target Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChallenge(nextUnsolved.id) },
                color = Panel,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorder),
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionTitle("Next Target")
                        BadgeView(
                            text = "${nextUnsolved.difficulty.label} · +${nextUnsolved.difficulty.points} pts",
                            color = difficultyColor(nextUnsolved.difficulty),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GlassOrb(tint = difficultyColor(nextUnsolved.difficulty), size = 42.dp) {
                            EvilIcon(
                                challengeIcon(nextUnsolved.id, nextUnsolved.area),
                                tint = difficultyColor(nextUnsolved.difficulty),
                                size = 20.dp,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = nextUnsolved.title,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            Text(
                                text = "${nextUnsolved.cwe} · ${nextUnsolved.masvs} · CVSS ${cvssNumericString(nextUnsolved.cvss)}",
                                fontSize = 11.5.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        EvilIcon(IconKind.ChevronRight, tint = Muted, size = 18.dp)
                    }
                }
            }
        }

        // 3. Attack Surface Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Attack Surface",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "View all ${all.size} →",
                        color = Muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onExploreAll),
                    )
                }

                for (row in Area.entries.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (area in row) {
                            val inArea = all.filter { it.area == area }
                            val doneInArea = inArea.count { store.isSolved(it.id) }
                            val areaPct = if (inArea.isEmpty()) 0f else doneInArea.toFloat() / inArea.size
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onExploreAll() },
                                color = Panel,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorder),
                            ) {
                                Column(
                                    modifier = Modifier.padding(13.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        EvilIcon(
                                            kind = challengeIcon("", area),
                                            tint = TextPrimary,
                                            size = 18.dp,
                                        )
                                        Text(
                                            text = "$doneInArea/${inArea.size}",
                                            fontSize = 11.sp,
                                            color = Muted,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = area.label,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.5.sp,
                                            color = TextPrimary,
                                        )
                                        Text(
                                            text = area.masvsCategory,
                                            fontSize = 10.5.sp,
                                            color = Muted,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Elevated),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(areaPct.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .background(if (doneInArea == inArea.size && inArea.isNotEmpty()) Green else TextPrimary),
                                        )
                                    }
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

        // 4. Featured Labs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Featured Labs",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "All Labs →",
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onExploreAll),
                )
            }
        }

        items(all.take(6), key = { it.id }) { c ->
            ChallengeRowCard(
                c = c,
                solved = store.isSolved(c.id),
                bookmarked = store.isBookmarked(c.id),
                onClick = { onOpenChallenge(c.id) },
            )
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            fontSize = 19.sp,
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

@Composable
fun ChallengeRowCard(
    c: Challenge,
    solved: Boolean,
    bookmarked: Boolean,
    onClick: () -> Unit,
) {
    val diffColor = difficultyColor(c.difficulty)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Panel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (solved) Green.copy(alpha = 0.35f) else CardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassOrb(
                tint = if (solved) Green else diffColor,
                size = 42.dp,
                cornerRadius = 10.dp,
            ) {
                EvilIcon(
                    kind = if (solved) IconKind.Check else challengeIcon(c.id, c.area),
                    tint = if (solved) Green else diffColor,
                    size = 20.dp,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = c.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (bookmarked) {
                        EvilIcon(IconKind.Bookmark, tint = Amber, size = 13.dp)
                    }
                }
                Text(
                    text = "${c.cwe} · ${c.masvs.removePrefix("MASVS-")} · CVSS ${cvssNumericString(c.cvss)}",
                    color = Muted,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BadgeView(
                    text = if (solved) "Solved" else c.difficulty.label,
                    color = if (solved) Green else diffColor,
                )
                Text(
                    text = "${c.difficulty.points} pts",
                    fontSize = 11.sp,
                    color = Muted,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Security Labs",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    "${filtered.size} of ${ChallengeCatalog.all.size} challenges · ${store.solved.size} solved",
                    fontSize = 12.5.sp,
                    color = Muted,
                )
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 18.dp) },
                placeholder = {
                    Text(
                        "Search title, CWE, MASVS...",
                        color = Muted,
                        fontSize = 13.sp,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel,
                    focusedBorderColor = TextPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )
        }

        // Single clean filter strip
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

        items(filtered, key = { it.id }) { c ->
            ChallengeRowCard(
                c = c,
                solved = store.isSolved(c.id),
                bookmarked = store.isBookmarked(c.id),
                onClick = { onOpenChallenge(c.id) },
            )
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "OWASP MASVS v2",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    "Verification coverage across mobile security domains and MASWE identifiers.",
                    fontSize = 12.5.sp,
                    color = Muted,
                )
            }
        }
        itemsIndexed(Area.entries) { _, area ->
            val inArea = all.filter { it.area == area }
            val done = inArea.count { store.isSolved(it.id) }
            val pct = if (inArea.isEmpty()) 0f else done.toFloat() / inArea.size
            Card {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GlassOrb(tint = TextPrimary, size = 36.dp, cornerRadius = 8.dp) {
                            EvilIcon(challengeIcon("", area), tint = TextPrimary, size = 18.dp)
                        }
                        Column {
                            Text(
                                area.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                            )
                            Text(
                                area.masvsCategory,
                                fontSize = 11.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                    BadgeView(
                        "$done / ${inArea.size}",
                        if (done == inArea.size && inArea.isNotEmpty()) Green else TextSoft,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Elevated),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(if (done == inArea.size && inArea.isNotEmpty()) Green else TextPrimary),
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
                            EvilIcon(IconKind.Check, tint = Green, size = 16.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Muted.copy(alpha = 0.5f), CircleShape),
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                c.title,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "${c.masvs} · ${c.maswe} · ${c.cwe}",
                                fontSize = 11.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        EvilIcon(IconKind.ChevronRight, tint = Muted, size = 15.dp)
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Progress & Telemetry",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    "Local assessment progress across difficulty tiers and MASVS domains.",
                    fontSize = 12.5.sp,
                    color = Muted,
                )
            }
        }

        item {
            StatRow(
                "Total Score" to "${store.score()}",
                "Labs Solved" to "$solvedCount/${all.size}",
                "Hints Used" to "$totalHints",
            )
        }

        item {
            Card {
                SectionTitle("By Difficulty Tier")
                Difficulty.entries.forEach { d ->
                    val list = all.filter { it.difficulty == d }
                    val done = list.count { store.isSolved(it.id) }
                    val pct = if (list.isEmpty()) 0f else done.toFloat() / list.size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${d.label} (${d.points} pts)",
                                fontSize = 13.sp,
                                color = difficultyColor(d),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "$done / ${list.size}",
                                fontSize = 12.5.sp,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Elevated),
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
        }

        item {
            Card {
                SectionTitle("By MASVS Domain")
                Area.entries.forEach { area ->
                    val list = all.filter { it.area == area }
                    val done = list.count { store.isSolved(it.id) }
                    val pct = if (list.isEmpty()) 0f else done.toFloat() / list.size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${area.label} (${area.masvsCategory})",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                "$done / ${list.size}",
                                fontSize = 12.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Elevated),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pct.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(if (done == list.size && list.isNotEmpty()) Green else TextPrimary),
                            )
                        }
                    }
                }
            }
        }
    }
}
