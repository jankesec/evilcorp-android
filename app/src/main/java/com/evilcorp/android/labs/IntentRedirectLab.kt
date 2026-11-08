package com.evilcorp.android.labs

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.Warning

/**
 * INTENTIONALLY INSECURE: exported proxy activity that forwards callers to any internal component
 * named in `target_class` (or nested `next_intent`), bypassing `android:exported="false"`.
 */
class IntentRedirectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        val nested = intent?.getParcelableExtra<Intent>("next_intent")
        val targetClass = intent?.getStringExtra("target_class")
        val forward = when {
            nested != null -> nested
            !targetClass.isNullOrBlank() -> Intent().apply {
                component = ComponentName(packageName, targetClass)
            }
            else -> null
        }
        if (forward != null) {
            runCatching {
                startActivity(forward)
                finish()
                return
            }
        }
        setContent {
            EvilCorpTheme {
                Column(
                    Modifier.fillMaxSize().background(Color.Black).systemBarsPadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Intent Redirect Router", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Mono("Missing 'target_class' or 'next_intent' extra.")
                }
            }
        }
    }
}

/** Non-exported internal admin screen; only reachable from inside the app's UID (or via redirect). */
class InternalAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EvilCorpTheme {
                Column(
                    Modifier.fillMaxSize().background(Color.Black).systemBarsPadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Internal Admin Console", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Warning("Non-exported activity reached via Intent Redirection!")
                    Mono("Admin console secret:\n${Flags.INTENTREDIRECT}", color = Green)
                }
            }
        }
    }
}

@Composable
fun IntentRedirectLab() {
    LabFrame(
        scenario = "InternalAdminActivity has android:exported=\"false\", so external callers cannot " +
            "launch it directly. However, IntentRedirectActivity is exported and blindly calls " +
            "startActivity() on whatever component name is passed in the 'target_class' string extra.",
        remediation = "Never forward untrusted nested Intents or arbitrary class names from exported " +
            "components. Validate the target against a strict allow-list of public destinations and " +
            "verify that resolved components have exported=true.",
    ) {
        Mono(
            "Exported proxy:  .labs.IntentRedirectActivity (exported=true)\n" +
                "Internal target: com.evilcorp.android.labs.InternalAdminActivity (exported=false)"
        )
    }
}
