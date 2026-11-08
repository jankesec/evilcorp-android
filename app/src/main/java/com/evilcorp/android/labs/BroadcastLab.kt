package com.evilcorp.android.labs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.GhostButton
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

private const val ROLE_PREFS = "evilcorp_role"

/** INTENTIONALLY INSECURE: exported, no permission, no caller verification. */
class PromoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val role = intent.getStringExtra("role") ?: return
        context.getSharedPreferences(ROLE_PREFS, Context.MODE_PRIVATE).edit().putString("role", role).apply()
        Log.i("EvilCorpRole", "role changed to $role by broadcast")
    }
}

@Composable
fun BroadcastLab() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(ROLE_PREFS, Context.MODE_PRIVATE) }
    fun current() = prefs.getString("role", "user") ?: "user"
    var role by remember { mutableStateOf(current()) }

    LabFrame(
        scenario = "An internal admin tool promotes accounts by broadcasting an intent. The receiver " +
            "is exported and does not check who sent it. Promote yourself to admin from outside the app, " +
            "then refresh.",
        remediation = "Don't export receivers that change state. If cross-app delivery is required, " +
            "protect them with a signature-level permission, and use explicit or local intents " +
            "for in-app communication.",
    ) {
        Mono(
            if (role == "admin") "role = admin\nAdmin console token: ${Flags.BROADCAST}"
            else "role = $role\nAdmin console locked."
        )
        PrimaryButton("Refresh") { role = current() }
        GhostButton("Reset role") {
            prefs.edit().putString("role", "user").apply()
            role = current()
        }
    }
}
