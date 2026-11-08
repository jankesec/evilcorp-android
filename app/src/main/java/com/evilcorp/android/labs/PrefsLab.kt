package com.evilcorp.android.labs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.PrimaryButton

const val PREFS_SESSION_FILE = "evilcorp_session"

@Composable
fun PrefsLab() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("No session stored yet.") }

    LabFrame(
        scenario = "The app \"remembers\" the signed-in user by saving the session token to " +
            "SharedPreferences. Anyone with access to the sandbox (root, backup, run-as on a " +
            "debuggable build) can read it.",
        remediation = "Never persist bearer tokens in plaintext. Keep them in memory, or encrypt them " +
            "with a non-exportable key from Android Keystore. Disable android:allowBackup or exclude " +
            "the file with dataExtractionRules.",
    ) {
        PrimaryButton("Sign in as alice") {
            // INTENTIONALLY INSECURE: plaintext token in shared_prefs/evilcorp_session.xml
            context.getSharedPreferences(PREFS_SESSION_FILE, Context.MODE_PRIVATE).edit()
                .putString("username", "alice")
                .putString("email", "alice@evilcorp.example")
                .putString("session_token", Flags.PREFS)
                .putBoolean("remember_me", true)
                .apply()
            status = "Session saved. Now find where it was saved."
        }
        Status(status)
    }
}
