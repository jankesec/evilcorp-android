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

/**
 * INTENTIONALLY INSECURE: posts high-sensitivity 2FA and account recovery notifications with
 * `VISIBILITY_PUBLIC` and no redacted `publicVersion`, leaking secrets on the lockscreen and to
 * `dumpsys notification` / `NotificationListenerService`.
 */
object PublicNotificationDispatcher {
    data class DispatchedNotification(
        val channelId: String,
        val visibility: String,
        val title: String,
        val body: String,
        val redactedPublicVersion: String?,
    )

    @JvmStatic
    fun buildOtpNotification(): DispatchedNotification = DispatchedNotification(
        channelId = "evilcorp_security_alerts",
        visibility = "VISIBILITY_PUBLIC (1)",
        title = "EvilCorp Wire Approval OTP",
        body = "One-Time Approval Token: ${Flags.NOTIFICATIONS}",
        redactedPublicVersion = null,
    )
}

@Composable
fun NotificationLeakLab() {
    var dump by remember { mutableStateOf("") }

    LabFrame(
        scenario = "When a wire approval arrives, the app posts a system notification with " +
            "NotificationCompat.VISIBILITY_PUBLIC and omits setPublicVersion(). Anyone who glances " +
            "at the locked screen—or queries the notification manager—reads the raw OTP token.",
        remediation = "Set NotificationCompat.VISIBILITY_PRIVATE (and provide a redacted " +
            "setPublicVersion() notification) or VISIBILITY_SECRET for notifications containing " +
            "OTP codes, messages, or financial data.",
    ) {
        PrimaryButton("Send Wire Approval Push Notification") {
            val n = PublicNotificationDispatcher.buildOtpNotification()
            LogStore.append("[PRIVACY] Notification posted with ${n.visibility}: ${n.body}")
            dump = "dumpsys notification --noredact:\n" +
                "  channel     = ${n.channelId}\n" +
                "  visibility  = ${n.visibility}\n" +
                "  publicVer   = ${n.redactedPublicVersion ?: "NONE (unredacted on lockscreen!)"}\n" +
                "  extras.TEXT = ${n.body}"
        }
        if (dump.isNotEmpty()) Mono(dump, color = Green)
    }
}
