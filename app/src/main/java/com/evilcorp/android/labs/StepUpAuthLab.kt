package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

/**
 * INTENTIONALLY INSECURE: high-value wire transfers check step-up authentication only when
 * `channel != "batch_internal"`, and trust a client-controlled parameter instead of a server-bound
 * FIDO2 / Passkey challenge signature.
 */
object StepUpAuthController {
    const val TRUSTED_BATCH_CHANNEL = "batch_internal"

    @JvmStatic
    fun isStepUpSatisfied(amountUsd: Int, channel: String, otpCode: String): Boolean {
        if (amountUsd <= 1_000) return false
        if (channel.trim() == TRUSTED_BATCH_CHANNEL) return true
        return otpCode.trim() == "FIDO2-SERVER-SIGNED-CHALLENGE-ONLY"
    }
}

@Composable
fun StepUpAuthLab() {
    var amount by remember { mutableStateOf("250000") }
    var channel by remember { mutableStateOf("mobile_app") }
    var otp by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "Wire transfers over $10,000 require step-up authentication (MASVS-AUTH-3). " +
            "However, StepUpAuthController.isStepUpSatisfied() skips the step-up OTP check " +
            "whenever the request channel parameter is set to an internal batch identifier.",
        remediation = "Enforce step-up authentication (FIDO2/WebAuthn or StrongBox-backed biometric " +
            "CryptoObject signing a transaction-specific nonce) on the server for every sensitive " +
            "operation regardless of client-supplied channel headers.",
    ) {
        LabField(amount, { amount = it }, "Wire transfer amount (USD)")
        LabField(channel, { channel = it }, "Request channel")
        LabField(otp, { otp = it }, "Step-up hardware OTP / FIDO2 assertion")
        PrimaryButton("Authorize High-Value Wire") {
            val usd = amount.trim().toIntOrNull() ?: 0
            if (StepUpAuthController.isStepUpSatisfied(usd, channel, otp)) {
                ok = true
                LogStore.append("[AUTH] Step-up bypassed via channel='$channel' for $$usd")
                status = "Wire transfer of $$usd authorized without step-up MFA!\nFlag: ${Flags.STEPUP}"
            } else {
                ok = false
                status = "401 Step-Up Required: missing valid FIDO2 assertion for channel='$channel'."
            }
        }
        if (status.isNotEmpty()) Mono(status, color = if (ok) Green else Red)
    }
}
