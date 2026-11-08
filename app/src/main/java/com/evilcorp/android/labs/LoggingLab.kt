package com.evilcorp.android.labs

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.PrimaryButton

@Composable
fun LoggingLab() {
    var user by remember { mutableStateOf("alice") }
    var pass by remember { mutableStateOf("wonderland") }
    var status by remember { mutableStateOf("") }

    LabFrame(
        scenario = "A developer left verbose debug logging in the authentication flow. The UI shows " +
            "nothing interesting, but the system log does.",
        remediation = "Strip debug logs from release builds (R8 -assumenosideeffects for android.util.Log, " +
            "or a logging wrapper gated on BuildConfig.DEBUG). Never log credentials, tokens or PII.",
    ) {
        LabField(user, { user = it }, "Username")
        LabField(pass, { pass = it }, "Password", secret = true)
        PrimaryButton("Login") {
            // INTENTIONALLY INSECURE: credentials and token written to logcat and console
            Log.d("EvilCorpAuth", "login attempt user=$user pass=$pass")
            Log.d("EvilCorpAuth", "login ok user=$user token=${Flags.LOGGING}")
            LogStore.append("[EvilCorpAuth] login attempt user=$user pass=$pass")
            LogStore.append("[EvilCorpAuth] login ok user=$user token=${Flags.LOGGING}")
            status = "Signed in. (Nothing to see here... on screen.)"
        }
        if (status.isNotEmpty()) Status(status, ok = true)
    }
}
