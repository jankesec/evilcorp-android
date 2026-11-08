package com.evilcorp.android.labs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red
import java.io.File

/** INTENTIONALLY WEAK: a purely client-side, single-boolean root check. */
object RootChecker {
    private val suPaths = listOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/app/Superuser.apk",
        "/data/local/xbin/su", "/data/adb/magisk",
    )

    /** Plants a synthetic indicator so the check fires even on a stock device/emulator. */
    fun plantIndicator(context: Context) {
        File(context.filesDir, "fake-root").apply { mkdirs() }.resolve("su").apply {
            if (!exists()) writeText("#!/system/bin/sh\n# synthetic EvilCorp lab indicator\n")
        }
    }

    fun indicators(context: Context): List<String> =
        (suPaths + File(context.filesDir, "fake-root/su").path).filter { File(it).exists() }

    @JvmStatic
    fun isRooted(context: Context): Boolean = indicators(context).isNotEmpty()
}

@Composable
fun RootLab() {
    val context = LocalContext.current
    var result by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The secure area refuses to open on rooted devices. A synthetic su indicator is " +
            "planted in the sandbox so the check always triggers. Make the app believe the device is clean.",
        remediation = "Client-side checks only raise the cost of attack; they can always be patched or " +
            "hooked. Use Play Integrity API with server-side verdict validation, layer multiple checks, " +
            "and never rely on the device to protect server-side assets.",
    ) {
        PrimaryButton("Open secure area") {
            RootChecker.plantIndicator(context)
            if (RootChecker.isRooted(context)) {
                ok = false
                result = "ACCESS DENIED: rooted device.\nIndicators:\n" +
                    RootChecker.indicators(context).joinToString("\n") { "  $it" }
            } else {
                ok = true
                result = "Device looks clean. Secure area: ${Flags.ROOT}"
            }
        }
        if (result.isNotEmpty()) Mono(result, color = if (ok) com.evilcorp.android.ui.Green else Red)
    }
}
