package com.evilcorp.android.labs

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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

private const val PI_PREFS = "evilcorp_pending_intent"

/**
 * Simulates an external receiver that intercepts a broadcast carrying a mutable PendingIntent,
 * mutates the fill-in action/extras, and fires it back with the original creator's identity.
 */
class PendingIntentHijackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        @Suppress("DEPRECATION")
        val pi = intent.getParcelableExtra<PendingIntent>("callback_pi")
        val mode = intent.getStringExtra("hijack_mode") ?: "intercept"
        if (pi != null || mode == "hijack") {
            val fillIn = Intent().apply {
                putExtra("hijacked_by", "external_receiver")
                putExtra("leaked_token", Flags.PENDINGINTENT)
            }
            runCatching { pi?.send(context, 0, fillIn) }
            context.getSharedPreferences(PI_PREFS, Context.MODE_PRIVATE).edit()
                .putString("captured", "Hijacked mutable PendingIntent! Token: ${Flags.PENDINGINTENT}")
                .apply()
            Log.w("EvilCorpPI", "Mutable PendingIntent hijacked -> token=${Flags.PENDINGINTENT}")
        }
    }
}

@Composable
fun PendingIntentLab() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PI_PREFS, Context.MODE_PRIVATE) }
    var status by remember {
        mutableStateOf(prefs.getString("captured", "No PendingIntent dispatched yet.") ?: "")
    }

    LabFrame(
        scenario = "The app creates a PendingIntent wrapping an implicit base Intent with " +
            "FLAG_MUTABLE and sends it in a broadcast. Any receiver that intercepts it (or invokes " +
            "PendingIntentHijackReceiver) can fill in the target fields and execute with EvilCorp's " +
            "identity and attached token.",
        remediation = "Always use PendingIntent.FLAG_IMMUTABLE unless a specific system feature " +
            "(such as inline notification replies) requires mutability, and always wrap an explicit " +
            "Intent with a concrete component and package name.",
    ) {
        PrimaryButton("Broadcast mutable PendingIntent") {
            val baseIntent = Intent("com.evilcorp.android.INTERNAL_CALLBACK").apply {
                putExtra("delegated_flag", Flags.PENDINGINTENT)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            // INTENTIONALLY INSECURE: implicit base Intent + FLAG_MUTABLE
            val pi = PendingIntent.getBroadcast(context, 1001, baseIntent, flags)
            context.sendBroadcast(
                Intent("com.evilcorp.android.ACTION_DISPATCH_PI").apply {
                    putExtra("callback_pi", pi)
                }
            )
            status = "Dispatched mutable PendingIntent (action=ACTION_DISPATCH_PI).\n" +
                "Trigger hijack receiver via adb and refresh."
        }
        PrimaryButton("Refresh capture log") {
            status = prefs.getString("captured", "No hijack recorded yet.") ?: ""
        }
        GhostButton("Reset") {
            prefs.edit().clear().apply()
            status = "Reset."
        }
        Mono(status)
    }
}
