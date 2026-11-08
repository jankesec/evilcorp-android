package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

/**
 * INTENTIONALLY INSECURE: demonstrates StrandHogg / Task Affinity hijacking where an activity leaves
 * the default package `taskAffinity` and enables `allowTaskReparenting="true"`, allowing a malicious
 * activity with the same `taskAffinity` to insert itself at the top of the victim task stack.
 */
object TaskStackInspector {
    const val VULNERABLE_AFFINITY = "com.evilcorp.android"

    @JvmStatic
    fun simulateTaskReparenting(taskAffinity: String, allowReparenting: Boolean): String {
        return if (taskAffinity == VULNERABLE_AFFINITY && allowReparenting) {
            "TaskRecord{affinity=$taskAffinity}:\n" +
                "  [1] com.attacker.phish/.FakeVaultLoginActivity (reparented on top!)\n" +
                "  [0] com.evilcorp.android/.MainActivity\n" +
                "Intercepted credentials from hijacked backstack: ${Flags.STRANDHOGG}"
        } else {
            "TaskRecord{affinity='${taskAffinity.ifEmpty { "<empty>" }}', allowTaskReparenting=$allowReparenting}:\n" +
                "  [0] com.evilcorp.android/.MainActivity (isolated task stack)"
        }
    }
}

@Composable
fun StrandHoggLab() {
    var affinity by remember { mutableStateOf(TaskStackInspector.VULNERABLE_AFFINITY) }
    var allowReparent by remember { mutableStateOf(true) }
    var output by remember { mutableStateOf("") }
    var hijacked by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "Because the manifest leaves android:taskAffinity set to the package name and " +
            "enables android:allowTaskReparenting=\"true\", another app declaring the same " +
            "taskAffinity can reparent a spoofed login screen onto EvilCorp's task backstack.",
        remediation = "Set android:taskAffinity=\"\" on <application> or sensitive <activity> " +
            "declarations, set android:allowTaskReparenting=\"false\", and use explicit launchMode " +
            "settings so foreign activities cannot join your task stack.",
    ) {
        LabField(affinity, { affinity = it }, "Manifest android:taskAffinity")
        PrimaryButton("allowTaskReparenting: ${if (allowReparent) "true (Vulnerable)" else "false (Hardened)"}") {
            allowReparent = !allowReparent
        }
        PrimaryButton("Launch App & Inspect ActivityManager Backstack") {
            val res = TaskStackInspector.simulateTaskReparenting(affinity.trim(), allowReparent)
            hijacked = res.contains(Flags.STRANDHOGG)
            if (hijacked) LogStore.append("[PLATFORM] Task stack hijacked via taskAffinity=$affinity")
            output = res
        }
        if (output.isNotEmpty()) Mono(output, color = if (hijacked) Green else Red)
    }
}
