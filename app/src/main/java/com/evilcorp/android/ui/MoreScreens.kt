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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker("ENGINEERING & DIAGNOSTICS")
                Text(
                    "Operator Toolkit",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }
        item {
            MoreMenuCard(
                index = "01",
                category = "runtime telemetry",
                title = "Live Security Console",
                subtitle = "Inspect real-time auth, SQL, IPC and exploit events (${LogStore.entries.size} lines)",
                badge = "LIVE",
                badgeColor = Green,
                onClick = { onNavigate(MorePage.Console) },
            )
        }
        item {
            MoreMenuCard(
                index = "02",
                category = "environment audit",
                title = "Device Intelligence",
                subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) · ${Build.MODEL} · Root & debug posture",
                badge = "API ${Build.VERSION.SDK_INT}",
                badgeColor = Mono100,
                onClick = { onNavigate(MorePage.Device) },
            )
        }
        item {
            MoreMenuCard(
                index = "03",
                category = "instrumentation",
                title = "Frida, Resources & Settings",
                subtitle = "Searchable ${ChallengeCatalog.all.size}-script Frida/ADB library, OWASP MASTG links and progress reset",
                badge = "${ChallengeCatalog.all.size} SCRIPTS",
                badgeColor = Accent,
                onClick = { onNavigate(MorePage.Extras) },
            )
        }
    }
}

@Composable
private fun MoreMenuCard(
    index: String,
    category: String,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel)
            .border(1.dp, CardBorder, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                Box(Modifier.size(5.dp).clip(CircleShape).background(Mono100))
                Text(
                    text = category.uppercase(),
                    color = Mono100,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp,
                )
            }
            Text(
                text = index,
                color = Muted,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                color = Muted,
                lineHeight = 18.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BadgeView(badge, badgeColor)
            Text(
                text = "↗",
                color = Mono100,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
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
        EvilIcon(IconKind.ChevronLeft, tint = Mono100, size = 18.dp)
        Spacer(Modifier.width(4.dp))
        Text(
            text = backLabel,
            fontSize = 14.sp,
            color = Mono100,
            fontWeight = FontWeight.Medium,
        )
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
            .padding(horizontal = 16.dp),
    ) {
        SubpageBackHeader(onBack = onBack)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Console",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            GhostButton("Test Log") {
                LogStore.append("[TEST] Console active · PID: ${Process.myPid()}")
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Panel)
                    .border(1.dp, LineStrong, RoundedCornerShape(6.dp))
                    .clickable { LogStore.clear() }
                    .padding(8.dp),
            ) {
                EvilIcon(IconKind.Trash, tint = Red, size = 16.dp)
            }
        }

        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            singleLine = true,
            leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 16.dp) },
            placeholder = {
                Text("Filter log stream...", color = Muted, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Panel,
                unfocusedContainerColor = Panel,
                focusedBorderColor = Mono100,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
        )

        val shape = RoundedCornerShape(8.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 14.dp)
                .clip(shape)
                .background(CodeBg)
                .border(1.dp, CardBorder, shape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Elevated)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Mono200))
                Spacer(Modifier.width(5.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(Mono500))
                Spacer(Modifier.width(5.dp))
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF4D4D4D)))
                Spacer(Modifier.width(10.dp))
                Text(
                    "logcat — com.evilcorp.android",
                    color = Muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${filtered.size} LINES",
                    color = Mono100,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                itemsIndexed(filtered) { _, line ->
                    val color = when {
                        line.contains("ERROR") || line.contains("DENIED") -> Red
                        line.contains("[TEST]") || line.contains("[SOLVED]") -> Green
                        line.contains("[HINT]") || line.contains("[CTF]") -> Accent
                        else -> TextSoft
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { SubpageBackHeader(onBack = onBack) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker("CHECKSEC // POSTURE")
                Text(
                    "Device Intelligence",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
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
                rows.forEachIndexed { idx, (key, value) ->
                    if (idx > 0) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(LineSubtle))
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            key.uppercase(),
                            color = Muted,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(126.dp),
                        )
                        Text(
                            value,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
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
            .padding(horizontal = 16.dp),
    ) {
        SubpageBackHeader(onBack = onBack)
        SegmentedControl(tabs = tabs, selectedIndex = tab, onSelect = { tab = it })
        Spacer(Modifier.height(10.dp))
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
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Panel)
            .border(1.dp, CardBorder, shape)
            .padding(3.dp),
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (selected) Mono100 else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.uppercase(),
                    color = if (selected) Bg else Muted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                )
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
            leadingIcon = { EvilIcon(IconKind.Search, tint = Muted, size = 16.dp) },
            placeholder = { Text("Search scripts...", color = Muted, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Panel,
                unfocusedContainerColor = Panel,
                focusedBorderColor = Mono100,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Text("${filtered.size} SCRIPTS", color = Muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(6.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { c ->
                LpCodeWindow(
                    title = "${c.cwe} · ${c.title}",
                    actionText = if (copiedId == c.id) "COPIED" else "COPY",
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText(c.id, c.tooling))
                        copiedId = c.id
                    },
                ) {
                    Text(
                        text = c.tooling,
                        color = TextSoft,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                    )
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
                Kicker(header)
            }
            items(links, key = { it.first }) { (title, url) ->
                val shape = RoundedCornerShape(6.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Panel)
                        .border(1.dp, CardBorder, shape)
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        },
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(title, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                            Text(url, color = Muted, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text("↗", color = Mono100, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
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
                    EvilIcon(IconKind.Trash, tint = Red, size = 18.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reset Progress", color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
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
                    EvilIcon(IconKind.Code, tint = Mono100, size = 18.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("GitHub Repository", color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("github.com/jankesec/evilcorp-android", color = Muted, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text("↗", color = Mono100, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
        item {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EvilIcon(IconKind.Shield, tint = Mono100, size = 18.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Build Info", color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "v${BuildConfig.VERSION_NAME} · ${ChallengeCatalog.all.size} Challenges · OWASP MASVS v2",
                            color = Muted,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}
