package com.evilcorp.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.ChallengeCatalog

private enum class Tab(val label: String, val icon: IconKind) {
    Home("Home", IconKind.Home),
    Labs("Labs", IconKind.Grid),
    Masvs("MASVS", IconKind.Checklist),
    Progress("Progress", IconKind.Chart),
    More("More", IconKind.More),
}

@Composable
fun EvilCorpApp(store: ProgressStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }

    val open = openId?.let(ChallengeCatalog::byId)
    if (open != null) {
        BackHandler { openId = null }
        ChallengeDetailScreen(
            c = open,
            store = store,
            onBack = { openId = null },
            onSelectChallenge = { openId = it },
        )
        return
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            Column(Modifier.fillMaxWidth()) {
                HorizontalDivider(color = CardBorder, thickness = 1.dp)
                NavigationBar(
                    containerColor = Color(0xFF0C0C12),
                    tonalElevation = 0.dp,
                ) {
                    Tab.entries.forEach { t ->
                        val selected = tab == t
                        NavigationBarItem(
                            selected = selected,
                            onClick = { tab = t },
                            icon = {
                                EvilIcon(
                                    kind = t.icon,
                                    tint = if (selected) Color.White else Muted,
                                    size = 21.dp,
                                )
                            },
                            label = {
                                Text(
                                    t.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                indicatorColor = Color.White.copy(alpha = 0.12f),
                                selectedTextColor = Color.White,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                            ),
                        )
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
