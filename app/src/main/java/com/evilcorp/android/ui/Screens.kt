package com.evilcorp.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.BuildConfig
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.Area
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Scoring

private val all = ChallengeCatalog.all

@Composable
private fun Screen(modifier: Modifier, title: String, subtitle: String, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 6.dp)) {
                Text(title, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(subtitle, fontSize = 13.sp, color = Muted)
            }
        }
        content()
    }
}

@Composable
fun ChallengeRow(c: Challenge, solved: Boolean, onOpen: (String) -> Unit) {
    Card(Modifier.clickable { onOpen(c.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                Text(c.description, fontSize = 12.sp, color = Muted, maxLines = 2)
            }
            Spacer(Modifier.width(8.dp))
            Text(if (solved) "✓" else "›", fontSize = 20.sp, color = if (solved) Green else Muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(c.difficulty.label, difficultyColor(c.difficulty))
            Chip(c.area.label)
            Chip(c.masvs, Muted)
        }
    }
}

@Composable
fun HomeScreen(store: ProgressStore, modifier: Modifier, onOpen: (String) -> Unit, onBrowse: () -> Unit) {
    val next = all.firstOrNull { !store.solved.containsKey(it.id) }
    Screen(modifier, "EvilCorp", "Android security lab · v${BuildConfig.VERSION_NAME}") {
        item {
            Warning(
                "Deliberately vulnerable. Use only on an emulator or a disposable test device. " +
                    "Never enter real credentials or personal data.",
            )
        }
        item {
            StatRow(
                "Labs" to "${all.size}",
                "Solved" to "${store.solved.size}",
                "Score" to "${store.score()}",
            )
        }
        item {
            Card {
                SectionTitle("Workflow")
                Text(
                    "1. Pick a lab  2. Reproduce with adb, Frida or jadx  3. Use hints only if stuck " +
                        "(−${Scoring.HINT_PENALTY} pts each)  4. Submit the EVILCORP{…} flag  5. Write down the fix.",
                    fontSize = 13.sp,
                )
                PrimaryButton("Browse labs", onClick = onBrowse)
            }
        }
        if (next != null) {
            item { SectionTitle("Up next") }
            item { ChallengeRow(next, false, onOpen) }
        }
    }
}

@Composable
fun LabsScreen(store: ProgressStore, modifier: Modifier, onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf<Area?>(null) }
    val filtered = all.filter { c ->
        (area == null || c.area == area) && (query.isBlank() || listOf(c.title, c.description, c.cwe, c.masvs, c.maswe)
            .any { it.contains(query, ignoreCase = true) })
    }
    Screen(modifier, "Labs", "${filtered.size} of ${all.size} challenges") {
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text("Search title, CWE, MASVS, MASWE") }, modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = area == null, onClick = { area = null }, label = { Text("All") })
                Area.entries.filter { a -> all.any { it.area == a } }.forEach { a ->
                    FilterChip(selected = area == a, onClick = { area = if (area == a) null else a }, label = { Text(a.label) })
                }
            }
        }
        items(filtered, key = { it.id }) { ChallengeRow(it, store.solved.containsKey(it.id), onOpen) }
    }
}

@Composable
fun MasvsScreen(store: ProgressStore, modifier: Modifier, onOpen: (String) -> Unit) {
    val groups = all.groupBy { it.masvs.substringBefore('-') }.toSortedMap()
    Screen(modifier, "MASVS", "OWASP MASVS v2 control groups covered by the labs") {
        groups.forEach { (group, list) ->
            val done = list.count { store.solved.containsKey(it.id) }
            item(key = "h-$group") {
                Column(Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("MASVS-$group", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("$done/${list.size}", color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { done.toFloat() / list.size },
                        modifier = Modifier.fillMaxWidth(), color = Green, trackColor = Panel,
                    )
                }
            }
            items(list, key = { "m-${it.id}" }) { ChallengeRow(it, store.solved.containsKey(it.id), onOpen) }
        }
    }
}

@Composable
fun ProgressScreen(store: ProgressStore, modifier: Modifier) {
    val max = Scoring.maxScore(all)
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    Screen(modifier, "Progress", "Stored locally on this device only") {
        item { StatRow("Score" to "${store.score()}", "Max" to "$max", "Hints" to "${store.hints.values.sum()}") }
        item {
            Card {
                SectionTitle("Overall")
                LinearProgressIndicator(
                    progress = { store.solved.size.toFloat() / all.size },
                    modifier = Modifier.fillMaxWidth(), color = Green, trackColor = Color(0xFF222222),
                )
                Text("${store.solved.size} / ${all.size} labs solved", fontSize = 12.sp, color = Muted)
            }
        }
        item { SectionTitle("By area") }
        Area.entries.filter { a -> all.any { it.area == a } }.forEach { a ->
            val list = all.filter { it.area == a }
            val done = list.count { store.solved.containsKey(it.id) }
            item(key = a.name) {
                Card {
                    Row {
                        Text(a.label, color = Color.White, modifier = Modifier.weight(1f))
                        Text("$done/${list.size}", color = Muted)
                    }
                    LinearProgressIndicator(
                        progress = { done.toFloat() / list.size },
                        modifier = Modifier.fillMaxWidth(), color = Green, trackColor = Color(0xFF222222),
                    )
                }
            }
        }
        item {
            if (confirmReset) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton("Yes, reset") { store.reset(); confirmReset = false }
                    GhostButton("Cancel") { confirmReset = false }
                }
            } else {
                GhostButton("Reset progress") { confirmReset = true }
            }
        }
    }
}
