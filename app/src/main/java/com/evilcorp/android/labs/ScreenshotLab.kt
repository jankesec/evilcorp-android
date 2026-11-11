package com.evilcorp.android.labs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import kotlinx.coroutines.delay

private const val REVEAL_MS = 250L

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
fun ScreenshotLab() {
    val activity = LocalContext.current.activity()
    var revealed by remember { mutableStateOf(false) }
    var secure by remember { mutableStateOf(false) }

    LaunchedEffect(revealed) {
        if (revealed) {
            // Far too short to read by eye, long enough for any screen capture without FLAG_SECURE.
            delay(REVEAL_MS)
            revealed = false
        }
    }
    DisposableEffect(secure) {
        if (secure) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    LabFrame(
        scenario = "The card screen flashes the full card secret for a fraction of a second, too fast " +
            "to read. Without FLAG_SECURE, screenshots, screen recordings, casting and the Recents " +
            "thumbnail can all capture it. Capture the secret, then turn on the fix and try again.",
        remediation = "Set WindowManager.LayoutParams.FLAG_SECURE on windows that show sensitive data " +
            "(and on dialogs/bottom sheets, which have their own windows). On API 33+ also consider " +
            "setRecentsScreenshotEnabled(false).",
    ) {
        Mono(if (revealed) "Card secret: ${Flags.SCREENSHOT}" else "Card secret: ●●●● ●●●● ●●●● ●●●●")
        PrimaryButton("Flash card secret (${REVEAL_MS} ms)") { revealed = true }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = secure, onCheckedChange = { secure = it })
            Text(if (secure) "Fix applied: FLAG_SECURE on" else "FLAG_SECURE off (vulnerable)")
        }
    }
}
