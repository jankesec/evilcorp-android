package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import java.io.File

@Composable
fun ExternalStorageLab() {
    val context = LocalContext.current
    var path by remember { mutableStateOf("") }

    LabFrame(
        scenario = "\"Export backup\" writes the user's credentials to app-specific external storage. " +
            "That location is readable over adb/MTP without root, and on older Android versions by any " +
            "app with storage permission. Retrieve the backup.",
        remediation = "Keep sensitive files in internal storage (filesDir / noBackupFilesDir), encrypt " +
            "anything that must leave the sandbox, and use the Storage Access Framework for user exports.",
    ) {
        PrimaryButton("Export backup") {
            val dir = context.getExternalFilesDir(null)
            if (dir != null) {
                // INTENTIONALLY INSECURE: secrets on shared/external storage
                val f = File(dir, "backup_credentials.txt")
                f.writeText("user=alice\npassword=wonderland\nrecovery=${Flags.EXTERNAL}\n")
                path = "Backup written to:\n${f.absolutePath}"
            } else {
                path = "External storage unavailable."
            }
        }
        if (path.isNotEmpty()) Mono(path)
    }
}
