package com.evilcorp.android.labs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.ui.EvilCorpTheme
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.Warning

/** INTENTIONALLY INSECURE: exported activity that trusts every query parameter. */
class DeepLinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data
        val (title, body) = when (uri?.host) {
            "settings" -> if (uri.getQueryParameter("debug") == "1") {
                "Debug settings unlocked" to "Hidden debug menu enabled via deep link.\nBuild secret: ${Flags.DEEPLINK}"
            } else {
                "Settings" to "Nothing to see here."
            }
            "pay" -> {
                val to = uri.getQueryParameter("to") ?: "?"
                val amount = uri.getQueryParameter("amount") ?: "0"
                "Transfer executed" to "Sent $amount EC$ to '$to' without asking the user.\n(Simulated, no network.)"
            }
            else -> "Unknown link" to (uri?.toString() ?: "no data")
        }
        setContent {
            EvilCorpTheme {
                Column(
                    Modifier.fillMaxSize().background(Color.Black).systemBarsPadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Warning("Opened by an external intent: ${uri ?: "-"}")
                    Mono(body)
                }
            }
        }
    }
}

@Composable
fun DeepLinkLab() {
    LabFrame(
        scenario = "EvilCorp registers the evilcorp:// scheme so marketing emails can open screens. " +
            "The handler is exported, accepts any parameters, and performs actions without " +
            "confirmation. Any app or web page can fire it. Unlock the hidden debug screen.",
        remediation = "Treat deep-link data as untrusted input: validate host, path and parameters " +
            "against an allow-list, never perform state-changing actions without explicit user " +
            "confirmation, and prefer verified App Links (https + assetlinks.json) over custom schemes.",
    ) {
        Mono("Registered: evilcorp://<host>?<params>\nHandler:    .labs.DeepLinkActivity (exported)")
    }
}
