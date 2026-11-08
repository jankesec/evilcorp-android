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

const val BACKUP_CONFIG_FILE = "cloud_sync_config.json"

@Composable
fun BackupLab() {
    val context = LocalContext.current
    var output by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The manifest declares android:allowBackup=\"true\" with no dataExtractionRules " +
            "or fullBackupContent exclusions, and the app writes a plaintext JSON configuration file " +
            "containing the cloud sync bearer token into internal storage.",
        remediation = "Set android:allowBackup=\"false\" or configure android:dataExtractionRules to " +
            "exclude sensitive files, place non-backup secrets in Context.getNoBackupFilesDir(), " +
            "and encrypt sensitive files with Jetpack Security EncryptedFile.",
    ) {
        PrimaryButton("Write cloud sync config") {
            // INTENTIONALLY INSECURE: plaintext config included in app backups and sandbox dumps
            val file = File(context.filesDir, BACKUP_CONFIG_FILE)
            file.writeText(
                """
                {
                  "endpoint": "https://sync.evilcorp.example/v2",
                  "account": "alice@evilcorp.example",
                  "auto_backup": true,
                  "sync_bearer_token": "${Flags.BACKUP}"
                }
                """.trimIndent() + "\n"
            )
            output = "Written to files/$BACKUP_CONFIG_FILE (${file.length()} bytes).\n" +
                "Manifest has android:allowBackup=\"true\"."
        }
        if (output.isNotEmpty()) Mono(output)
    }
}
