package com.evilcorp.android.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.BuildConfig
import com.evilcorp.android.R
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.Area
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Difficulty
import com.evilcorp.android.model.Scoring

private val all = ChallengeCatalog.all

@Composable
fun HomeScreen(
    store: ProgressStore,
    modifier: Modifier,
    onOpen: (String) -> Unit,
    onBrowse: () -> Unit,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    var selectedArea by rememberSaveable { mutableStateOf<Area?>(null) }

    val featured = remember {
        listOf("sqli", "sslpinning", "intentredirect", "biometric", "webview", "pathtraversal")
            .mapNotNull(ChallengeCatalog::byId)
    }
    val nextChallenge = remember(store.solved.size) {
        all.firstOrNull { !store.solved.containsKey(it.id) } ?: all.first()
    }
    val filteredGrid = remember(selectedArea, showAll) {
        val base = if (selectedArea == null) all else all.filter { it.area == selectedArea }
        if (showAll || selectedArea != null) base else base.take(8)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (store.ctfMode) {
            item { CtfBanner(store) }
        }

        // Clean Hero Header with ambient radial glow
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(160.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Green.copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width / 2f,
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.evilcorp_logo),
                            contentDescription = "EvilCorp emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Text(
                        text = "EVILCORP",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.5.sp,
                        color = Color.White,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Android Security Laboratory",
                            fontSize = 13.sp,
                            color = Muted,
                        )
                        BadgeView("v${BuildConfig.VERSION_NAME}", Green)
                    }
                }
            }
        }

        // 4 Stat cards row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCardView(
                    value = "${all.size}",
                    label = "Challenges",
                    icon = IconKind.Flask,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f),
                )
                StatCardView(
                    value = "${store.solved.size}",
                    label = "Solved",
                    icon = IconKind.Check,
                    iconColor = Green,
                    modifier = Modifier.weight(1f),
                )
                StatCardView(
                    value = "${store.score()}",
                    label = "Points",
                    icon = IconKind.Chart,
                    iconColor = Accent,
                    modifier = Modifier.weight(1f),
                )
                StatCardView(
                    value = if (store.ctfMode) "CTF" else "Lab",
                    label = "Mode",
                    icon = if (store.ctfMode) IconKind.Flag else IconKind.Shield,
                    iconColor = if (store.ctfMode) Accent else Blue,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Active Mission / Up Next + CTF Mode Action Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(nextChallenge.id) },
                color = Panel,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Green)
                            )
                            Text(
                                text = "NEXT TARGET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp,
                                color = Green,
                            )
                        }
                        if (!store.ctfMode) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Accent.copy(alpha = 0.16f))
                                    .clickable { store.startCtf() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                EvilIcon(IconKind.Flag, tint = Accent, size = 12.dp)
                                Text(
                                    "START CTF MODE",
                                    color = Accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Elevated),
                            contentAlignment = Alignment.Center,
                        ) {
                            EvilIcon(
                                kind = challengeIcon(nextChallenge.id, nextChallenge.area),
                                tint = Color.White,
                                size = 20.dp,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = nextChallenge.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = "${nextChallenge.cwe} · ${nextChallenge.masvs} · ${nextChallenge.difficulty.points} pts",
                                fontSize = 11.5.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        BadgeView(nextChallenge.difficulty.label, difficultyColor(nextChallenge.difficulty))
                        Spacer(Modifier.width(6.dp))
                        EvilIcon(IconKind.ChevronRight, tint = Muted, size = 16.dp)
                    }
                }
            }
        }

        // Featured horizontal carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Featured Labs",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "High-impact scenarios",
                        fontSize = 12.sp,
                        color = Muted,
                    )
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(featured, key = { "feat-${it.id}" }) { c ->
                        FeaturedCardView(
                            challenge = c,
                            solved = store.solved.containsKey(c.id),
                            onClick = { onOpen(c.id) },
                        )
                    }
                }
            }
        }

        // All Vulnerabilities header + Category filter chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "All Vulnerabilities",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "Search & filter ›",
                        fontSize = 13.sp,
                        color = Accent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(onClick = onBrowse),
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterPill(
                        text = "All (${all.size})",
                        selected = selectedArea == null,
                        color = Color.White,
                        onClick = { selectedArea = null },
                    )
                    Area.entries.forEach { a ->
                        val count = all.count { it.area == a }
                        FilterPill(
                            text = "${a.label} ($count)",
                            selected = selectedArea == a,
                            color = Color.White,
                            onClick = { selectedArea = if (selectedArea == a) null else a },
                        )
                    }
                }
            }
        }

        items(filteredGrid.chunked(2), key = { "grid-${it.first().id}" }) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pair.forEach { c ->
                    GlassCardView(
                        challenge = c,
                        solved = store.solved.containsKey(c.id),
                        modifier = Modifier.weight(1f),
                        onClick = { onOpen(c.id) },
                    )
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        if (!showAll && selectedArea == null) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAll = true },
                    color = Panel,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder),
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Show all ${all.size} challenges",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCardView(
    challenge: Challenge,
    solved: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(224.dp)
            .clickable(onClick = onClick),
        color = Panel,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (solved) Green.copy(alpha = 0.35f) else CardBorder,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background((if (solved) Green else Color.White).copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    EvilIcon(
                        kind = challengeIcon(challenge.id, challenge.area),
                        tint = if (solved) Green else Color.White,
                        size = 19.dp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    BadgeView("${challenge.difficulty.points} pts", Muted)
                    BadgeView(
                        text = challenge.difficulty.label,
                        color = difficultyColor(challenge.difficulty),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = challenge.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = challenge.description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Muted,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${challenge.cwe} · ${challenge.masvs}",
                    fontSize = 10.5.sp,
                    color = Muted,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = if (solved) "Solved ✓" else "Launch ›",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (solved) Green else Accent,
                )
            }
        }
    }
}

@Composable
private fun GlassCardView(
    challenge: Challenge,
    solved: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (solved) Green.copy(alpha = 0.35f) else CardBorder,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background((if (solved) Green else Color.White).copy(alpha = 0.09f)),
                    contentAlignment = Alignment.Center,
                ) {
                    EvilIcon(
                        kind = challengeIcon(challenge.id, challenge.area),
                        tint = if (solved) Green else Color.White,
                        size = 17.dp,
                    )
                }
                if (solved) {
                    EvilIcon(IconKind.Check, tint = Green, size = 16.dp)
                } else {
                    BadgeView(challenge.cwe, Muted)
                }
            }
            Text(
                text = challenge.title,
                fontSize = 13.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                BadgeView(challenge.difficulty.label, difficultyColor(challenge.difficulty))
                BadgeView(challenge.masvs, Muted)
            }
        }
    }
}

@Composable
fun ChallengeRow(c: Challenge, solved: Boolean, onOpen: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(c.id) },
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (solved) Green.copy(alpha = 0.30f) else CardBorder,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background((if (solved) Green else Color.White).copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                EvilIcon(
                    kind = challengeIcon(c.id, c.area),
                    tint = if (solved) Green else Color.White,
                    size = 20.dp,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = c.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = Color.White,
                )
                Text(
                    text = "${c.cwe} · ${c.masvs} · ${c.maswe} · ${c.cvss}",
                    fontSize = 11.sp,
                    color = Muted,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BadgeView(c.difficulty.label, difficultyColor(c.difficulty))
                if (solved) {
                    EvilIcon(IconKind.Check, tint = Green, size = 16.dp)
                } else {
                    Text(
                        "${c.difficulty.points} pts",
                        fontSize = 10.sp,
                        color = Muted,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
fun LabsScreen(store: ProgressStore, modifier: Modifier, onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var diff by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var tag by rememberSaveable { mutableStateOf<String?>(null) }
    var onlyBookmarked by rememberSaveable { mutableStateOf(false) }

    val quickTags = remember {
        listOf(
            "CWE-89", "CWE-312", "CWE-798", "CWE-319", "CWE-327", "CWE-200", "CWE-927",
            "STORAGE-1", "STORAGE-2", "PLATFORM-1", "PLATFORM-2", "CRYPTO-1", "AUTH-2", "RESILIENCE-1",
        )
    }

    val filtered = all.filter { c ->
        (!onlyBookmarked || store.isBookmarked(c.id)) &&
            (diff == null || c.difficulty == diff) &&
            (tag == null || c.cwe == tag || c.masvs == tag) &&
            (query.isBlank() || listOf(c.title, c.description, c.cwe, c.masvs, c.maswe, c.area.label)
                .any { it.contains(query, ignoreCase = true) })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
    ) {
        // Search box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Panel,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorder),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EvilIcon(IconKind.Search, tint = Muted, size = 18.dp)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Search CWE-89, STORAGE-1...", color = Muted, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { query = "" }
                            .padding(2.dp),
                    ) {
                        EvilIcon(IconKind.Close, tint = Muted, size = 16.dp)
                    }
                }
            }
        }

        // Difficulty + Bookmarked filter pills
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill(
                "All",
                selected = diff == null && !onlyBookmarked,
                color = Color.White,
            ) {
                diff = null
                onlyBookmarked = false
            }
            Difficulty.entries.forEach { d ->
                FilterPill(
                    text = d.label,
                    selected = diff == d,
                    color = difficultyColor(d),
                    onClick = { diff = if (diff == d) null else d },
                )
            }
            FilterPill(
                text = "Saved",
                selected = onlyBookmarked,
                color = Accent,
                onClick = { onlyBookmarked = !onlyBookmarked },
            )
        }

        // Taxonomy tag pills
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            quickTags.forEach { t ->
                TagPill(
                    text = t,
                    selected = tag == t,
                    onClick = { tag = if (tag == t) null else t },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filtered, key = { it.id }) { c ->
                ChallengeRow(c, store.solved.containsKey(c.id), onOpen)
            }
        }
    }
}

@Composable
fun MasvsScreen(store: ProgressStore, modifier: Modifier, onOpen: (String) -> Unit) {
    var expandedControl by rememberSaveable { mutableStateOf<String?>(null) }
    val families = remember {
        listOf(
            Triple("STORAGE", "Local Data Storage & Privacy", listOf("STORAGE-1", "STORAGE-2")),
            Triple("CRYPTO", "Cryptography & Key Management", listOf("CRYPTO-1", "CRYPTO-2")),
            Triple("NETWORK", "Network Communication & TLS", listOf("NETWORK-1", "NETWORK-2")),
            Triple("AUTH", "Authentication & Biometrics", listOf("AUTH-2")),
            Triple("PLATFORM", "Platform IPC, WebViews & UI", listOf("PLATFORM-1", "PLATFORM-2", "PLATFORM-3")),
            Triple("RESILIENCE", "Reverse Engineering & Tampering", listOf("RESILIENCE-1", "RESILIENCE-2", "RESILIENCE-4")),
            Triple("CODE", "Injection & Input Sanitization", listOf("CODE-4")),
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "OWASP MASVS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(families, key = { it.first }) { (code, subtitle, controls) ->
            val familyLabs = all.filter { it.masvs.startsWith(code) }
            Card {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(code, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${familyLabs.size} labs", color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(subtitle, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                controls.forEach { ctrl ->
                    val ctrlLabs = all.filter { it.masvs == ctrl }
                    val isExpanded = expandedControl == ctrl
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expandedControl = if (isExpanded) null else ctrl }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (ctrlLabs.isNotEmpty()) Green else Muted),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(ctrl, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(Green.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "${ctrlLabs.size}",
                                    color = Green,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                        if (isExpanded) {
                            ctrlLabs.forEach { lab ->
                                ChallengeRow(lab, store.solved.containsKey(lab.id), onOpen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(store: ProgressStore, modifier: Modifier) {
    val solvedCount = store.solved.size
    val totalCount = all.size
    val leftCount = totalCount - solvedCount
    val maxPoints = Scoring.maxScore(all)
    val pct = if (totalCount > 0) (solvedCount * 100) / totalCount else 0
    val animatedFraction by animateFloatAsState(
        targetValue = if (totalCount > 0) solvedCount.toFloat() / totalCount.toFloat() else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "ring",
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "Progress",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // Circular Progress Ring
        item {
            Box(
                modifier = Modifier
                    .size(164.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke,
                    )
                    if (animatedFraction > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(listOf(Green, Accent, Color.White, Green)),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedFraction,
                            useCenter = false,
                            style = stroke,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$solvedCount",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text("of $totalCount", fontSize = 13.sp, color = Muted)
                }
            }
        }

        // 3 Stat pills: Completed, Left, Score %
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProgressPill(
                    icon = IconKind.Check,
                    value = "$solvedCount",
                    label = "Completed",
                    color = Green,
                    modifier = Modifier.weight(1f),
                )
                ProgressPill(
                    icon = IconKind.Clock,
                    value = "$leftCount",
                    label = "Left",
                    color = Accent,
                    modifier = Modifier.weight(1f),
                )
                ProgressPill(
                    icon = IconKind.Chart,
                    value = "$pct%",
                    label = "${store.score()} / $maxPoints pts",
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Achievements card
        item {
            Card {
                Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Area.entries.forEach { a ->
                        val catLabs = all.filter { it.area == a }
                        val catDone = catLabs.count { store.solved.containsKey(it.id) }
                        val unlocked = catLabs.isNotEmpty() && catDone == catLabs.size
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (unlocked) Green.copy(alpha = 0.18f)
                                        else Color.White.copy(alpha = 0.06f)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                EvilIcon(
                                    kind = if (unlocked) IconKind.Check else IconKind.Lock,
                                    tint = if (unlocked) Green else Muted,
                                    size = 22.dp,
                                )
                            }
                            Text(
                                a.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (unlocked) Color.White else Muted,
                            )
                            Text(
                                "$catDone/${catLabs.size}",
                                fontSize = 10.sp,
                                color = if (unlocked) Green else Muted,
                            )
                        }
                    }
                }
            }
        }

        // Category breakdown bars
        item {
            Card {
                Area.entries.forEach { a ->
                    val list = all.filter { it.area == a }
                    val done = list.count { store.solved.containsKey(it.id) }
                    val frac = if (list.isNotEmpty()) done.toFloat() / list.size else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(a.label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Text("$done/${list.size}", color = Muted, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(frac)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(Green, Color.White))
                                    ),
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "v${BuildConfig.VERSION_NAME} · EvilCorp Android",
                fontSize = 11.sp,
                color = Muted,
            )
        }
    }
}

@Composable
private fun ProgressPill(
    icon: IconKind,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Panel,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            EvilIcon(icon, tint = color, size = 20.dp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = Muted, maxLines = 1)
        }
    }
}
