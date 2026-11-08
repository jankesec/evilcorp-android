package com.evilcorp.android.labs

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.evilcorp.android.ui.PrimaryButton
import java.io.File

const val WEBVIEW_SECRET_FILE = "internal_audit.txt"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewFileLab() {
    val context = LocalContext.current
    var url by remember { mutableStateOf("https://help.evilcorp.example/index.html") }
    var targetUrl by remember { mutableStateOf(url) }

    LaunchedEffect(Unit) {
        File(context.filesDir, WEBVIEW_SECRET_FILE).writeText(
            "CONFIDENTIAL INTERNAL AUDIT\nFinding #19: WebView file:// access enabled.\nFlag: ${Flags.WEBVIEWFILE}\n"
        )
    }

    LabFrame(
        scenario = "The in-app documentation viewer enables allowFileAccess and " +
            "allowUniversalAccessFromFileURLs, and loads arbitrary URLs supplied to it. Read the " +
            "private file internal_audit.txt inside the app's files/ directory using the WebView.",
        remediation = "Set settings.allowFileAccess = false, allowFileAccessFromFileURLs = false, and " +
            "allowUniversalAccessFromFileURLs = false. Serve local assets via WebViewAssetLoader over " +
            "https://appassets.androidplatform.net/ and block file:// schemes in shouldOverrideUrlLoading.",
    ) {
        LabField(url, { url = it }, "Document URL")
        PrimaryButton("Load URL") { targetUrl = url.trim() }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(190.dp),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    // INTENTIONALLY INSECURE: permits file:// URLs to read sandbox files
                    settings.allowFileAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                }
            },
            update = { wv ->
                if (targetUrl.startsWith("file://")) {
                    wv.loadUrl(targetUrl)
                } else {
                    wv.loadDataWithBaseURL(
                        targetUrl,
                        "<html><body style='background:#111;color:#eee;font-family:monospace'>" +
                            "<h4>EvilCorp Help Viewer</h4><p>Loaded: $targetUrl</p>" +
                            "<p>Tip: local offline docs live in the app sandbox.</p></body></html>",
                        "text/html",
                        "utf-8",
                        null,
                    )
                }
            },
        )
    }
}
