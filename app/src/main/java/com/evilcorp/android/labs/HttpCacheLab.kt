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

const val HTTP_CACHE_DIR = "http_cache"
const val HTTP_CACHE_ENTRY = "entry_statement_v1.0"

@Composable
fun HttpCacheLab() {
    val context = LocalContext.current
    var output by remember { mutableStateOf("") }

    LabFrame(
        scenario = "When fetching the user's account statement, the HTTP client caches the full " +
            "response on disk under cache/http_cache/ because Cache-Control: no-store was omitted. " +
            "Even after the screen masks the sensitive audit note, the raw response stays on disk.",
        remediation = "Send Cache-Control: no-store, no-cache, Pragma: no-cache on sensitive API " +
            "responses, disable disk caching on authenticated HTTP clients for sensitive endpoints, " +
            "and purge the HTTP cache on logout.",
    ) {
        PrimaryButton("Fetch /v1/account/statement") {
            // INTENTIONALLY INSECURE: sensitive API response written verbatim to disk cache
            val dir = File(context.cacheDir, HTTP_CACHE_DIR).apply { mkdirs() }
            val entry = File(dir, HTTP_CACHE_ENTRY)
            entry.writeText(
                """
                GET https://api.evilcorp.example/v1/account/statement
                HTTP/1.1 200 OK
                Cache-Control: max-age=86400
                Content-Type: application/json

                {"account":"EC-9941-002","balance":"48,200.00 USD","audit_token":"${Flags.HTTPCACHE}"}
                """.trimIndent() + "\n"
            )
            output = "Account: EC-9941-002\nBalance: 48,200.00 USD\nAudit token: [REDACTED IN UI]\n" +
                "(Cached on disk in cache/$HTTP_CACHE_DIR/$HTTP_CACHE_ENTRY)"
        }
        if (output.isNotEmpty()) Mono(output)
    }
}
