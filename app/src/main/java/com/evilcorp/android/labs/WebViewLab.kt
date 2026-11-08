package com.evilcorp.android.labs

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.evilcorp.android.ui.PrimaryButton

/** INTENTIONALLY INSECURE: native object exposed to any script in the WebView. */
class EvilBridge {
    @JavascriptInterface
    fun getFlag(): String = Flags.WEBVIEW

    @JavascriptInterface
    fun getDeviceId(): String = "EC-DEVICE-0000-SYNTHETIC"
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebViewLab() {
    var name by remember { mutableStateOf("") }
    var html by remember { mutableStateOf(page("guest")) }

    LabFrame(
        scenario = "The profile preview renders your display name as HTML inside a WebView. The " +
            "WebView has JavaScript enabled and exposes a native bridge called EvilBridge.",
        remediation = "HTML-encode untrusted input, keep JavaScript disabled unless required, never " +
            "expose @JavascriptInterface objects to content you do not fully control, and restrict " +
            "navigation to an allow-list of origins.",
    ) {
        LabField(name, { name = it }, "Display name")
        PrimaryButton("Preview") { html = page(name) }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            factory = { ctx ->
                WebView.setWebContentsDebuggingEnabled(true)
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    addJavascriptInterface(EvilBridge(), "EvilBridge")
                }
            },
            update = { it.loadDataWithBaseURL("https://profile.evilcorp.example/", html, "text/html", "utf-8", null) },
        )
    }
}

// INTENTIONALLY INSECURE: no output encoding.
private fun page(name: String) =
    "<html><body style='background:#111;color:#eee;font-family:sans-serif'>" +
        "<h3>Hello, $name</h3><p>Your profile preview.</p></body></html>"
