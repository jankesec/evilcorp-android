package com.evilcorp.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.sp
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.model.ChallengeCatalog

private enum class Tab(val label: String, val glyph: String) {
    Home("Home", "⌂"),
    Labs("Labs", "☰"),
    Masvs("MASVS", "◈"),
    Progress("Progress", "★"),
}

@Composable
fun EvilCorpApp(store: ProgressStore) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var openId by rememberSaveable { mutableStateOf<String?>(null) }

    val open = openId?.let(ChallengeCatalog::byId)
    if (open != null) {
        BackHandler { openId = null }
        ChallengeDetailScreen(open, store, onBack = { openId = null })
        return
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0B0B0B)) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Text(t.glyph, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                        ),
                    )
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
        }
    }
}

