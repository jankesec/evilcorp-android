package com.evilcorp.android.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.BuildConfig
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.data.ProgressStore
import com.evilcorp.android.labs.RootChecker
import com.evilcorp.android.model.ChallengeCatalog

private enum class MorePage { Menu, Console, Device, Extras }

@Composable
fun MoreScreen(store: ProgressStore, modifier: Modifier = Modifier) {
    var page by rememberSaveable { mutableStateOf(MorePage.Menu) }

    if (page != MorePage.Menu) {
        BackHandler { page = MorePage.Menu }
    }

    when (page) {
        MorePage.Menu -> MoreMenuView(modifier, onNavigate = { page = it })
        MorePage.Console -> ConsoleView(modifier, onBack = { page = MorePage.Menu })
        MorePage.Device -> DeviceIntelligenceView(modifier, onBack = { page = MorePage.Menu })
        MorePage.Extras -> ExtrasView(store, modifier, onBack = { page = MorePage.Menu })
    }
}

@Composable
private fun MoreMenuView(modifier: Modifier, onNavigate: (MorePage) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("More", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        MoreMenuRow(IconKind.Terminal, "Console") { onNavigate(MorePage.Console) }
        HorizontalDivider(color = CardBorder)
        MoreMenuRow(IconKind.Phone, "Device") { onNavigate(MorePage.Device) }
        HorizontalDivider(color = CardBorder)
        MoreMenuRow(IconKind.More, "Extras") { onNavigate(MorePage.Extras) }
        HorizontalDivider(color = CardBorder)
    }
}

@Composable
private fun MoreMenuRow(icon: IconKind, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EvilIcon(icon, tint = Color(0xFFD4D4DC), size = 22.dp)
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 17.sp, color = Color.White, modifier = Modifier.weight(1f))
        EvilIcon(IconKind.ChevronRight, tint = Muted, size = 16.dp)
    }
}

@Composable
private fun SubpageBackHeader(backLabel: String = "More", onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBack)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EvilIcon(IconKind.ChevronLeft, tint = Color.White, size = 20.dp)
        Spacer(Modifier.width(4.dp))
        Text(backLabel, fontSize = 17.sp, color = Color.White)
    }
}

@Composable
private fun ConsoleView(modifier: Modifier, onBack: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("") }
    val filtered = LogStore.entries.filter {
        filter.isBlank() || it.contains(filter, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 16.dp),
    ) {
        SubpageBackHeader(onBack = onBack)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Console",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { LogStore.clear() }
                    .padding(8.dp),
            ) {
                EvilIcon(IconKind.Trash, tint = Red, size = 20.dp)
            }
        }

        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            singleLine = true,
            leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 18.dp) },
            placeholder = { Text("Filter...", color = Muted) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        LogStore.append("[TEST] Console active · PID: ${Process.myPid()}")
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("▶", color = Green, fontSize = 12.sp)
                Text("Test Log", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(filtered) { _, line ->
                val color = when {
                    line.contains("ERROR") || line.contains("DENIED") -> Red
                    line.contains("[TEST]") || line.contains("[SOLVED]") -> Green
                    line.contains("[HINT]") || line.contains("[CTF]") -> Accent
                    else -> Color(0xFFD0D0DC)
                }
                Text(
                    text = line,
                    color = color,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun DeviceIntelligenceView(modifier: Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isEmulator = remember {
        Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.MODEL.contains("sdk", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true) ||
            Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }
    val isDebuggable = remember {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val rooted = remember { RootChecker.indicators(context).isNotEmpty() }
    val rows = remember {
        listOf(
            "Device Name" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "Model" to Build.MODEL,
            "System" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Build ID" to Build.DISPLAY,
            "Environment" to if (isEmulator) "Emulator (${Build.HARDWARE})" else "Physical Device",
            "Root Indicators" to if (rooted) "Detected (su / test-keys)" else "None found",
            "Debuggable" to if (isDebuggable) "Yes (FLAG_DEBUGGABLE)" else "No",
            "ABIs" to Build.SUPPORTED_ABIS.joinToString(", "),
            "Screen" to "${config.screenWidthDp}x${config.screenHeightDp} dp",
            "Density" to "${config.densityDpi} dpi",
            "App PID" to "${Process.myPid()}",
            "App Sandbox" to context.filesDir.parentFile?.absolutePath.orEmpty(),
            "Package ID" to context.packageName,
        )
    }

    LaunchedEffect(Unit) {
        LogStore.append("[DEVICE] Collected intelligence: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), PID ${Process.myPid()}")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { SubpageBackHeader(onBack = onBack) }
        item {
            Text(
                "Device Intelligence",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        items(rows, key = { it.first }) { (key, value) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Panel,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        key,
                        color = Muted,
                        fontSize = 14.sp,
                        modifier = Modifier.width(126.dp),
                    )
                    Text(
                        value,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtrasView(store: ProgressStore, modifier: Modifier, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Frida", "Resources", "Settings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 16.dp),
    ) {
        SubpageBackHeader(onBack = onBack)
        SegmentedControl(tabs = tabs, selectedIndex = tab, onSelect = { tab = it })
        Spacer(Modifier.height(8.dp))
        when (tab) {
            0 -> FridaLibrarySubView()
            1 -> ResourcesSubView()
            2 -> SettingsSubView(store)
        }
    }
}

@Composable
fun SegmentedControl(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Panel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Elevated else Color.Transparent)
                        .clickable { onSelect(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else Muted,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun FridaLibrarySubView() {
    val context = LocalContext.current
    var search by rememberSaveable { mutableStateOf("") }
    var copiedId by remember { mutableStateOf<String?>(null) }
    val filtered = ChallengeCatalog.all.filter {
        search.isBlank() ||
            it.title.contains(search, ignoreCase = true) ||
            it.tooling.contains(search, ignoreCase = true) ||
            it.cwe.contains(search, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            singleLine = true,
            leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 18.dp) },
            placeholder = { Text("Search scripts...", color = Muted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text("${filtered.size} scripts", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { c ->
                Card {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                            Text(
                                "${c.cwe} · ${c.masvs} · ${c.maswe}",
                                fontSize = 11.sp,
                                color = Muted,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Elevated)
                                .clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText(c.id, c.tooling))
                                    copiedId = c.id
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(
                                if (copiedId == c.id) "Copied" else "Copy",
                                color = if (copiedId == c.id) Green else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Mono(c.tooling, color = Accent)
                }
            }
        }
    }
}

@Composable
private fun ResourcesSubView() {
    val context = LocalContext.current
    val sections = remember {
        listOf(
            "Standards & Guides" to listOf(
                "OWASP MASVS" to "https://mas.owasp.org/MASVS/",
                "OWASP MASTG" to "https://mas.owasp.org/MASTG/",
                "OWASP MASWE" to "https://mas.owasp.org/MASWE/",
                "Android App Security Best Practices" to "https://developer.android.com/privacy-and-security/security-best-practices",
            ),
            "Dynamic & Static Analysis" to listOf(
                "Frida Dynamic Instrumentation" to "https://frida.re/docs/android/",
                "Objection Mobile Exploration" to "https://github.com/sensepost/objection",
                "jadx Dex to Java Decompiler" to "https://github.com/skylot/jadx",
                "Apktool Reverse Engineering" to "https://apktool.org/",
                "mitmproxy Intercepting Proxy" to "https://mitmproxy.org/",
            ),
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        sections.forEach { (header, links) ->
            item {
                Text(header, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(links, key = { it.first }) { (title, url) ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        },
                    color = Panel,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorder),
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(url, color = Muted, fontSize = 11.sp)
                        }
                        EvilIcon(IconKind.ChevronRight, tint = Muted, size = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSubView(store: ProgressStore) {
    val context = LocalContext.current
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { confirmReset = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EvilIcon(IconKind.Trash, tint = Red, size = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reset Progress", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Clear all solved labs, hints, bookmarks and CTF score", color = Muted, fontSize = 12.sp)
                    }
                }
                if (confirmReset) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton("Confirm Reset") {
                            store.reset()
                            confirmReset = false
                        }
                        GhostButton("Cancel") { confirmReset = false }
                    }
                }
            }
        }
        item {
            Card(
                Modifier.clickable {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jankesec/evilcorp-android"))
                        )
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EvilIcon(IconKind.Code, tint = Color.White, size = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("GitHub Repository", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("github.com/jankesec/evilcorp-android", color = Muted, fontSize = 12.sp)
                    }
                    EvilIcon(IconKind.ChevronRight, tint = Muted, size = 16.dp)
                }
            }
        }
        item {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EvilIcon(IconKind.Shield, tint = Green, size = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Build Info", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "v${BuildConfig.VERSION_NAME} · ${ChallengeCatalog.all.size} Challenges · OWASP MASVS v2",
                            color = Muted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}
