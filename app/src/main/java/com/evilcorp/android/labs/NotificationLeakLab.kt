package com.evilcorp.android.labs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

/**
 * INTENTIONALLY INSECURE: posts a wire-approval notification with VISIBILITY_PUBLIC and no redacted
 * public version, and attaches the approval token to the notification extras. Everything that can
 * read notifications (lock screen, NotificationListenerService, `dumpsys notification`) sees it.
 */
object PublicNotificationDispatcher {
    const val CHANNEL_ID = "evilcorp_security_alerts"
    const val NOTIFICATION_ID = 4141
    const val EXTRA_APPROVAL_TOKEN = "com.evilcorp.extra.APPROVAL_TOKEN"

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun post(context: Context): String {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Security alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                // INTENTIONALLY INSECURE: full content allowed on the lock screen
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
        val otp = (100_000..999_999).random().toString()
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("EvilCorp wire approval")
            .setContentText("Approve wire #4471 (48,200.00 USD) with code $otp")
            // INTENTIONALLY INSECURE: VISIBILITY_PUBLIC and no setPublicVersion() redaction
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            // INTENTIONALLY INSECURE: the long-lived approval token rides along in the extras
            .addExtras(Bundle().apply { putString(EXTRA_APPROVAL_TOKEN, Flags.NOTIFICATIONS) })
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
        return otp
    }
}

@Composable
fun NotificationLeakLab() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }

    fun send() {
        PublicNotificationDispatcher.post(context)
        LogStore.append("[PRIVACY] Wire approval notification posted (visibility=PUBLIC)")
        status = "Notification posted to channel '${PublicNotificationDispatcher.CHANNEL_ID}'.\n" +
            "The app itself never displays the approval token."
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) send() else status = "Notification permission denied. Allow it to run the lab."
    }

    LabFrame(
        scenario = "Wire approvals arrive as system notifications posted with VISIBILITY_PUBLIC and no " +
            "redacted public version. The approval token is attached to the notification itself. " +
            "Anything that can read notifications can read it. Recover the token without the app " +
            "ever showing it to you.",
        remediation = "Use VISIBILITY_PRIVATE with a redacted setPublicVersion() (or VISIBILITY_SECRET) " +
            "for OTPs, messages and financial data, never put tokens in notification extras or " +
            "PendingIntents, and keep notification text to the minimum the user needs.",
    ) {
        PrimaryButton("Send Wire Approval Notification") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !PublicNotificationDispatcher.canPost(context)
            ) {
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                send()
            }
        }
        if (status.isNotEmpty()) Mono(status)
    }
}
