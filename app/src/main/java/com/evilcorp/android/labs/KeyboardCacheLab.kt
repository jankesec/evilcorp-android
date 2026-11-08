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

const val IME_CACHE_FILE = "ime_learned_words.txt"

@Composable
fun KeyboardCacheLab() {
    val context = LocalContext.current
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The recovery phrase field enables predictive text suggestions and custom IME " +
            "dictionary learning instead of disabling suggestions and autofill. Previous entries " +
            "were cached into the personalized dictionary store.",
        remediation = "Mark sensitive inputs with KeyboardType.Password, disable personalized IME " +
            "learning (EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING), and configure " +
            "IMPORTANT_FOR_AUTOFILL_NO when autofill should not capture the field.",
    ) {
        LabField(note, { note = it }, "Recovery phrase (predictive text enabled)")
        PrimaryButton("Sync IME learned dictionary") {
            // INTENTIONALLY INSECURE: sensitive recovery tokens persisted in dictionary cache
            val f = File(context.filesDir, IME_CACHE_FILE)
            f.writeText(
                buildString {
                    appendLine("# EvilCorp IME Personalized Suggestion Cache v1")
                    appendLine("lang=en_US")
                    appendLine("word=evilcorp_corporate_vpn freq=12")
                    appendLine("word=${Flags.KEYBOARD} freq=9")
                    if (note.isNotBlank()) appendLine("word=${note.trim()} freq=1")
                }
            )
            status = "IME dictionary cache updated (${f.name})."
        }
        if (status.isNotEmpty()) Mono(status)
    }
}
