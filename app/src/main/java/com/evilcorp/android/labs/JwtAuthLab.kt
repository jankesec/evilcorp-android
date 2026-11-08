package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.GhostButton
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red
import java.util.Base64

/**
 * INTENTIONALLY INSECURE: trusts JWT claims client-side and accepts unsigned `alg:none` tokens
 * without verifying an HMAC/RSA signature on a backend server.
 */
object JwtSessionVerifier {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    val DEFAULT_GUEST_TOKEN: String = run {
        val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val payload = encoder.encodeToString("""{"sub":"j.doe","role":"employee","dept":"retail"}""".toByteArray())
        "$header.$payload.c2lnbmF0dXJlX25vdF9jaGVja2Vk"
    }

    fun forgeAlgNoneAdminToken(): String {
        val header = encoder.encodeToString("""{"alg":"none","typ":"JWT"}""".toByteArray())
        val payload = encoder.encodeToString("""{"sub":"j.doe","role":"admin","dept":"executive"}""".toByteArray())
        return "$header.$payload."
    }

    @JvmStatic
    fun isAdminToken(jwt: String): Boolean {
        val parts = jwt.trim().split(".")
        if (parts.size < 2) return false
        return runCatching {
            val headerJson = String(decoder.decode(parts[0]), Charsets.UTF_8)
            val payloadJson = String(decoder.decode(parts[1]), Charsets.UTF_8)
            val algNoneOrUnchecked = headerJson.contains("\"alg\"")
            val isAdmin = Regex(""""role"\s*:\s*"admin"""").containsMatchIn(payloadJson)
            algNoneOrUnchecked && isAdmin
        }.getOrDefault(false)
    }

    fun decodeClaimsPreview(jwt: String): String {
        val parts = jwt.trim().split(".")
        if (parts.size < 2) return "Malformed JWT (expected header.payload.signature)"
        return runCatching {
            val h = String(decoder.decode(parts[0]), Charsets.UTF_8)
            val p = String(decoder.decode(parts[1]), Charsets.UTF_8)
            "Header:  $h\nPayload: $p\nSig len: ${parts.getOrNull(2)?.length ?: 0} chars"
        }.getOrElse { "Base64URL decode error: ${it.message}" }
    }
}

@Composable
fun JwtAuthLab() {
    var token by remember { mutableStateOf(JwtSessionVerifier.DEFAULT_GUEST_TOKEN) }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The Executive Treasury view parses the session Bearer JWT directly on the client " +
            "using JwtSessionVerifier.isAdminToken() without verifying the cryptographic signature or " +
            "rejecting 'alg':'none'. Forge an unsigned admin JWT or hook the verifier.",
        remediation = "Never rely on client-side JWT claim inspection for authorization. Validate " +
            "signatures on the backend against a strict algorithm allow-list (reject 'none'), " +
            "and enforce short token lifetimes with server-side RBAC checks.",
    ) {
        LabField(token, { token = it }, "Bearer JWT (header.payload.sig)")
        Mono(JwtSessionVerifier.decodeClaimsPreview(token))
        PrimaryButton("Access Executive Treasury") {
            if (JwtSessionVerifier.isAdminToken(token)) {
                ok = true
                LogStore.append("[AUTH] Accepted forged admin JWT -> ${Flags.JWT}")
                output = "200 OK · Role=admin verified (signature check skipped)!\nFlag: ${Flags.JWT}"
            } else {
                ok = false
                LogStore.append("[AUTH] Denied JWT (role != admin)")
                output = "403 Forbidden: JWT claim 'role' is not 'admin'."
            }
        }
        GhostButton("Reset to employee token") {
            token = JwtSessionVerifier.DEFAULT_GUEST_TOKEN
            ok = false
            output = ""
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
