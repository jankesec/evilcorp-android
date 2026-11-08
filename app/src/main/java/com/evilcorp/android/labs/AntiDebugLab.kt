package com.evilcorp.android.labs

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Debug
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red
import java.io.File

/**
 * INTENTIONALLY WEAK: client-side debugger & instrumentation checks gated by a single boolean
 * method (and a QA bypass property left in bytecode).
 */
object AntiDebugCheck {
    const val QA_BYPASS_PROP = "debug.evilcorp.allow_debug"

    fun findings(context: Context): List<String> = buildList {
        val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (debuggable) add("ApplicationInfo.FLAG_DEBUGGABLE is set")
        if (Debug.isDebuggerConnected()) add("Debug.isDebuggerConnected() == true")
        val tracerPid = runCatching {
            File("/proc/self/status").useLines { lines ->
                lines.firstOrNull { it.startsWith("TracerPid:") }?.substringAfter(':')?.trim()?.toIntOrNull()
            }
        }.getOrNull() ?: 0
        if (tracerPid != 0) add("/proc/self/status TracerPid=$tracerPid")
    }

    private fun qaBypassEnabled(): Boolean =
        runCatching {
            ProcessBuilder("getprop", QA_BYPASS_PROP).start().inputStream.bufferedReader().readText().trim() == "1"
        }.getOrDefault(false)

    @JvmStatic
    fun isBeingDebugged(context: Context): Boolean {
        if (qaBypassEnabled()) return false
        return findings(context).isNotEmpty()
    }
}

@Composable
fun AntiDebugLab() {
    val context = LocalContext.current
    var output by remember { mutableStateOf("") }
    var clean by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The telemetry module refuses to release its signing key whenever it detects a " +
            "debuggable build or attached debugger via AntiDebugCheck.isBeingDebugged(). Neutralize " +
            "the check to retrieve the key.",
        remediation = "Never rely on client-side anti-debugging to protect high-value secrets; keep " +
            "secrets server-side, layer native and runtime integrity checks, and strip QA bypasses " +
            "before shipping.",
    ) {
        PrimaryButton("Run anti-debug check") {
            if (AntiDebugCheck.isBeingDebugged(context)) {
                clean = false
                output = "ANALYSIS DETECTED:\n" +
                    AntiDebugCheck.findings(context).joinToString("\n") { "  - $it" }
            } else {
                clean = true
                output = "Environment verified clean.\nTelemetry key: ${Flags.ANTIDEBUG}"
            }
        }
        if (output.isNotEmpty()) Mono(output, color = if (clean) Green else Red)
    }
}
