package com.evilcorp.android.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.ChallengeCatalog

private enum class Tab(val label: String, val hudSection: String, val icon: IconKind) {
    Home("Home", "home", IconKind.Home),
    Labs("Labs", "labs", IconKind.Grid),
    Masvs("MASVS", "masvs", IconKind.Checklist),
    Progress("Progress", "telemetry", IconKind.Chart),
    More("More", "toolkit", IconKind.More),
}

@Composable
fun EvilCorpApp(store: ProgressStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }

    val open = openId?.let(ChallengeCatalog::byId)
    if (open != null) {
        BackHandler { openId = null }
        LiquidBackdrop {
            ChallengeDetailScreen(
                c = open,
                store = store,
                onBack = { openId = null },
                onSelectChallenge = { openId = it },
            )
        }
        return
    }

    LiquidBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(Modifier.statusBarsPadding()) {
                    JankesecHeader(
                        section = tab.hudSection,
                        score = store.score(),
                        solvedCount = store.solved.size,
                        totalCount = ChallengeCatalog.all.size,
                    )
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xF2060606))
                        .navigationBarsPadding(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(LineSubtle),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Tab.entries.forEach { t ->
                            val selected = tab == t
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tab = t }
                                    .padding(bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(2.dp)
                                        .background(if (selected) Mono100 else Color.Transparent),
                                )
                                Spacer(Modifier.height(7.dp))
                                EvilIcon(
                                    kind = t.icon,
                                    tint = if (selected) Mono100 else Muted,
                                    size = 18.dp,
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = t.label.uppercase(),
                                    fontSize = 9.5.sp,
                                    color = if (selected) Mono100 else Muted,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.8.sp,
                                )
                            }
                        }
                    }
                }
            },
        ) { padding ->
            val m = Modifier.padding(padding)
            val onOpen: (String) -> Unit = { openId = it }
            when (tab) {
                Tab.Home -> HomeScreen(store, m, onOpen) { tab = Tab.Labs }
                Tab.Labs -> LabsScreen(store, m, onOpen)
                Tab.Masvs -> MasvsScreen(store, m, onOpen)
                Tab.Progress -> ProgressScreen(store, m)
                Tab.More -> MoreScreen(store, m)
            }
        }
    }
}
