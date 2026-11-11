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
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * INTENTIONALLY INSECURE: verifies HS256 signatures correctly, but also accepts tokens whose header
 * says `"alg": "none"` and treats them as trusted. The HMAC key is random per process and never
 * leaves memory, so editing the claims of a signed token fails: only the alg:none downgrade works.
 */
object JwtSessionVerifier {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()
    private val hmacKey: ByteArray by lazy { ByteArray(32).also { SecureRandom().nextBytes(it) } }

    private fun b64(json: String): String = encoder.encodeToString(json.toByteArray(Charsets.UTF_8))

    private fun hs256(signingInput: String): String =
        encoder.encodeToString(
            Mac.getInstance("HmacSHA256").run {
                init(SecretKeySpec(hmacKey, "HmacSHA256"))
                doFinal(signingInput.toByteArray(Charsets.UTF_8))
            }
        )

    /** The legitimate session token issued to the employee j.doe (properly signed). */
    val DEFAULT_GUEST_TOKEN: String by lazy {
        val signingInput = b64("""{"alg":"HS256","typ":"JWT"}""") + "." +
            b64("""{"sub":"j.doe","role":"employee","dept":"retail"}""")
        "$signingInput.${hs256(signingInput)}"
    }

    /** Reference exploit used by the unit tests. */
    fun forgeAlgNoneAdminToken(): String =
        b64("""{"alg":"none","typ":"JWT"}""") + "." + b64("""{"sub":"j.doe","role":"admin","dept":"executive"}""") + "."

    @JvmStatic
    fun isAdminToken(jwt: String): Boolean {
        val parts = jwt.trim().split(".")
        if (parts.size != 3) return false
        return runCatching {
            val header = String(decoder.decode(parts[0]), Charsets.UTF_8)
            val payload = String(decoder.decode(parts[1]), Charsets.UTF_8)
            val alg = Regex(""""alg"\s*:\s*"([^"]*)"""").find(header)?.groupValues?.get(1) ?: return false
            val trusted = when (alg.lowercase()) {
                "hs256" -> MessageDigest.isEqual(
                    hs256("${parts[0]}.${parts[1]}").toByteArray(Charsets.UTF_8),
                    parts[2].toByteArray(Charsets.UTF_8),
                )
                // INTENTIONALLY INSECURE: an unsigned token is trusted as if it had been verified
                "none" -> true
                else -> false
            }
            trusted && Regex(""""role"\s*:\s*"admin"""").containsMatchIn(payload)
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
        scenario = "The Executive Treasury view authorizes requests from the role claim of your session " +
            "JWT. The verifier checks HS256 signatures, so simply editing the claims of your token gets " +
            "rejected. Find another way to make it accept an admin token.",
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
                LogStore.append("[AUTH] Denied JWT (signature or role check failed)")
                output = "403 Forbidden: token rejected (invalid signature or role is not 'admin')."
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
