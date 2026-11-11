package com.evilcorp.android.labs

import java.security.MessageDigest

/**
 * Runtime access to the CTF flags.
 *
 * The APK holds no plaintext flags: `app/flags.properties` is compiled into [FlagVaultData]
 * (XOR-encoded bytes plus SHA-256 digests) at build time. A lab calls [reveal] only on its own
 * exploitation path, and submissions are checked with [matches], which compares digests in
 * constant time and never decodes the expected flag.
 *
 * This raises the bar from `strings classes.dex | grep EVILCORP{` to real reverse engineering.
 * A client-side CTF can never be fully cheat-proof: anyone who reverses this class or hooks it
 * with Frida can still pull every flag. Treat flags as learning checkpoints, not as proof.
 */
object FlagVault {
    val ids: List<String>
        get() = FlagVaultData.IDS.toList()

    fun reveal(id: String): String =
        requireNotNull(FlagVaultData.reveal(id)) { "Unknown lab id: $id" }

    fun matches(id: String, submitted: String): Boolean {
        val expected = FlagVaultData.digest(id) ?: return false
        val actual = MessageDigest.getInstance("SHA-256")
            .digest(submitted.trim().toByteArray(Charsets.UTF_8))
        return MessageDigest.isEqual(expected, actual)
    }
}
