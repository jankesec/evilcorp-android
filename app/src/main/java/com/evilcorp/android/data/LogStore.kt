package com.evilcorp.android.data

import android.os.Process
import androidx.compose.runtime.mutableStateListOf
import com.evilcorp.android.BuildConfig
import com.evilcorp.android.model.ChallengeCatalog

/** In-app live security console store (mirrors EvilCorp iOS Console). */
object LogStore {
    val entries = mutableStateListOf(
        "[EVILCORP] v${BuildConfig.VERSION_NAME} started · PID: ${Process.myPid()}",
        "[EVILCORP] ${ChallengeCatalog.all.size} training challenges loaded",
        "[EVILCORP] Cleartext HTTP enabled · Backup enabled · evilcorp:// scheme registered",
        "[INFO] Open any lab or run exploits to inspect live console events",
        "[INFO] Challenge actions log credentials, queries and tokens here",
    )

    fun append(message: String) {
        entries.add(message)
        if (entries.size > 250) {
            entries.removeRange(0, 100)
        }
    }

    fun clear() {
        entries.clear()
    }
}
