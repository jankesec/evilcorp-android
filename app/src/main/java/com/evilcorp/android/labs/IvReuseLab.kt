package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.evilcorp.android.ui.Mono
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * INTENTIONALLY INSECURE: real AES/CTR under one key with a hardcoded all-zero IV for every
 * message. The key is random per process and never leaves memory, so jadx cannot shortcut the
 * lab: the only offline path is the two-time pad P2 = C1 xor P1 xor C2.
 */
object NonceReuseCrypto {
    const val STATIC_IV_HEX = "00000000000000000000000000000000"
    const val KNOWN_PLAINTEXT = "GUEST_SESSION_HEADER_2026_pad_0000000"

    private val sessionKey: SecretKey by lazy { newKey() }

    fun newKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(128) }.generateKey()

    /** AES/CTR with the static IV: the bug is that every call reuses the same key + IV pair. */
    fun encrypt(key: SecretKey, plaintext: String): ByteArray =
        Cipher.getInstance("AES/CTR/NoPadding").run {
            init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(fromHex(STATIC_IV_HEX)))
            doFinal(plaintext.toByteArray(Charsets.UTF_8))
        }

    /** The two packets an on-path observer captures in this session. */
    fun interceptedPackets(): Pair<String, String> =
        toHex(encrypt(sessionKey, KNOWN_PLAINTEXT)) to toHex(encrypt(sessionKey, Flags.IVREUSE))

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    fun fromHex(hex: String): ByteArray {
        val clean = hex.trim().replace(" ", "")
        require(clean.length % 2 == 0) { "Odd hex length" }
        return ByteArray(clean.length / 2) { i -> clean.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }

    /** Reference solver used by the unit tests; the app UI never runs it for you. */
    @JvmStatic
    fun recoverViaTwoTimePad(c1Hex: String, p1Ascii: String, c2Hex: String): String {
        val c1 = fromHex(c1Hex)
        val p1 = p1Ascii.toByteArray(Charsets.UTF_8)
        val c2 = fromHex(c2Hex)
        val len = minOf(c1.size, p1.size, c2.size)
        return String(ByteArray(len) { i -> (c1[i].toInt() xor p1[i].toInt() xor c2[i].toInt()).toByte() }, Charsets.UTF_8)
    }
}

@Composable
fun IvReuseLab() {
    val (c1, c2) = remember { NonceReuseCrypto.interceptedPackets() }

    LabFrame(
        scenario = "Two session packets were captured on the wire. Both were encrypted with AES-CTR under " +
            "the same key and a hardcoded IV. The first one is a public greeting whose plaintext you know; " +
            "the second one carries the vault flag. The key never leaves the device. Recover the second " +
            "plaintext and submit it.",
        remediation = "Never hardcode or reuse IVs/nonces. Let Cipher.init() generate a fresh random IV for " +
            "every message (or use AES-GCM with a unique 96-bit nonce per key) and keep keys in " +
            "AndroidKeyStore.",
    ) {
        Mono(
            "IV (hex):    ${NonceReuseCrypto.STATIC_IV_HEX}\n" +
                "P1 (ascii):  ${NonceReuseCrypto.KNOWN_PLAINTEXT}\n" +
                "C1 (hex):    $c1\n" +
                "C2 (hex):    $c2"
        )
    }
}
