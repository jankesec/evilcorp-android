package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.GhostButton
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import java.io.File

@Composable
fun PathTraversalLab() {
    val context = LocalContext.current
    var entryName by remember { mutableStateOf("dark_theme.css") }
    var entryBody by remember { mutableStateOf("body { background: #000; }") }
    var output by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The theme importer extracts archive entries into files/themes/<entryName> by " +
            "joining paths without checking canonicalPath. Meanwhile, the admin vault unlocks " +
            "whenever files/admin_override.flag contains the word UNLOCK. Escape files/themes/ " +
            "using path traversal (Zip Slip) to overwrite files/admin_override.flag.",
        remediation = "Resolve the destination with file.canonicalPath and verify that it starts " +
            "with targetDir.canonicalPath + File.separator before writing; reject any entry " +
            "containing '..' or leading slashes.",
    ) {
        LabField(entryName, { entryName = it }, "Archive entry filename")
        LabField(entryBody, { entryBody = it }, "Archive entry content")
        PrimaryButton("Extract entry into files/themes/") {
            val themesDir = File(context.filesDir, "themes").apply { mkdirs() }
            // INTENTIONALLY INSECURE: no canonicalPath traversal check (Zip Slip)
            val dest = File(themesDir, entryName)
            dest.parentFile?.mkdirs()
            dest.writeText(entryBody.trim())

            val overrideFile = File(context.filesDir, "admin_override.flag")
            if (overrideFile.exists() && overrideFile.readText().trim() == "UNLOCK") {
                unlocked = true
                output = "Wrote: ${dest.canonicalPath}\n" +
                    "admin_override.flag == UNLOCK -> Vault open!\nFlag: ${Flags.PATHTRAVERSAL}"
            } else {
                unlocked = false
                output = "Wrote: ${dest.canonicalPath}\n" +
                    "(files/admin_override.flag is not set to UNLOCK yet)"
            }
        }
        GhostButton("Reset override file") {
            File(context.filesDir, "admin_override.flag").delete()
            unlocked = false
            output = "Reset files/admin_override.flag."
        }
        if (output.isNotEmpty()) {
            if (unlocked) Mono(output, color = Green) else Mono(output)
        }
    }
}
