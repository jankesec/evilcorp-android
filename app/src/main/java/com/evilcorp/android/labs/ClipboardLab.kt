package com.evilcorp.android.labs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

@Composable
fun ClipboardLab() {
    val context = LocalContext.current
    var pasted by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The recovery-code screen offers a convenient Copy button. The code goes to the " +
            "global clipboard with no sensitivity flag, so it shows up in clipboard previews, keyboards " +
            "and other apps. Recover the code without reading it on screen.",
        remediation = "Avoid copying secrets. If you must, set ClipDescription.EXTRA_IS_SENSITIVE " +
            "(API 33+), clear the clip after a short timeout, and disable copy on password fields.",
    ) {
        Mono("Recovery code: ●●●●●●●●●●●●●●●●●●●●")
        PrimaryButton("Copy recovery code") {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // INTENTIONALLY INSECURE: no EXTRA_IS_SENSITIVE, never cleared
            cm.setPrimaryClip(ClipData.newPlainText("recovery", Flags.CLIPBOARD))
            status = "Copied."
        }
        if (status.isNotEmpty()) Status(status)
        LabField(pasted, { pasted = it }, "Paste here (simulating another app)")
    }
}
