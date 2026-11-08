package com.evilcorp.android.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

@Composable
fun TapjackingLab() {
    var overlayActive by remember { mutableStateOf(true) }
    var filterObscured by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }

    LabFrame(
        scenario = "A malicious app draws a deceptive floating overlay on top of EvilCorp's " +
            "\"Approve Master Key Export\" button. Because filterTouchesWhenObscured is disabled " +
            "and FLAG_WINDOW_IS_OBSCURED is ignored, tapping the bait overlay passes straight " +
            "through to the sensitive action underneath.",
        remediation = "Set android:filterTouchesWhenObscured=\"true\" on sensitive views, check " +
            "MotionEvent.FLAG_WINDOW_IS_OBSCURED / FLAG_WINDOW_IS_PARTIALLY_OBSCURED, and call " +
            "window.setHideOverlayWindows(true) on Android 12 (API 31) and newer.",
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Switch(checked = overlayActive, onCheckedChange = { overlayActive = it })
            Text(if (overlayActive) "Simulated bait overlay: ON" else "Simulated bait overlay: OFF")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Switch(checked = filterObscured, onCheckedChange = { filterObscured = it })
            Text(if (filterObscured) "filterTouchesWhenObscured: ON (fixed)" else "filterTouchesWhenObscured: OFF (vulnerable)")
        }

        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1212), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("DANGER ZONE: Export Vault Master Key", color = Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                PrimaryButton("Approve Master Key Export") {
                    result = if (overlayActive && filterObscured) {
                        "Touch rejected: window is obscured by an overlay (FLAG_WINDOW_IS_OBSCURED)."
                    } else if (overlayActive) {
                        "TAPJACKED! Touch passed through bait overlay.\nExported key: ${Flags.TAPJACKING}"
                    } else {
                        "Direct click without overlay. Enable the bait overlay to test tapjacking."
                    }
                }
            }
            if (overlayActive) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xD9102A1E), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("FREE COFFEE COUPON! (Bait Overlay Window)", color = Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    PrimaryButton("Tap Here to Claim Free Coffee") {
                        result = if (filterObscured) {
                            "Touch blocked by filterTouchesWhenObscured=true!"
                        } else {
                            "TAPJACKED! Underlying 'Approve Master Key Export' triggered!\n" +
                                "Exported key: ${Flags.TAPJACKING}"
                        }
                    }
                }
            }
        }
        if (result.isNotEmpty()) Mono(result)
    }
}
