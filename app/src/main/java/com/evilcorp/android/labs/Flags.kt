package com.evilcorp.android.labs

/**
 * Synthetic CTF flags for all 42 labs, decoded at runtime from [FlagVault].
 *
 * These are getters, not `const val`s, on purpose: a constant would be inlined into the DEX as a
 * greppable string. The plaintext values live in `app/flags.properties` (maintainers only).
 * Only read a flag on the exploitation path of the lab it belongs to.
 */
object Flags {
    val PREFS: String get() = FlagVault.reveal("prefs")
    val SQLI: String get() = FlagVault.reveal("sqli")
    val SECRETS: String get() = FlagVault.reveal("secrets")
    val LOGGING: String get() = FlagVault.reveal("logging")
    val CRYPTO: String get() = FlagVault.reveal("crypto")
    val RANDOM: String get() = FlagVault.reveal("random")
    val WEBVIEW: String get() = FlagVault.reveal("webview")
    val PROVIDER: String get() = FlagVault.reveal("provider")
    val DEEPLINK: String get() = FlagVault.reveal("deeplink")
    val ROOT: String get() = FlagVault.reveal("root")
    val CLEARTEXT: String get() = FlagVault.reveal("cleartext")
    val EXTERNAL: String get() = FlagVault.reveal("external")
    val BROADCAST: String get() = FlagVault.reveal("broadcast")
    val CLIPBOARD: String get() = FlagVault.reveal("clipboard")
    val PIN: String get() = FlagVault.reveal("pin")
    val SCREENSHOT: String get() = FlagVault.reveal("screenshot")
    val KEYSTORE: String get() = FlagVault.reveal("keystore")
    val BACKUP: String get() = FlagVault.reveal("backup")
    val SSLPINNING: String get() = FlagVault.reveal("sslpinning")
    val WEBVIEWFILE: String get() = FlagVault.reveal("webviewfile")
    val BIOMETRIC: String get() = FlagVault.reveal("biometric")
    val ANTIDEBUG: String get() = FlagVault.reveal("antidebug")
    val KEYBOARD: String get() = FlagVault.reveal("keyboard")
    val TAMPERING: String get() = FlagVault.reveal("tampering")
    val HTTPCACHE: String get() = FlagVault.reveal("httpcache")
    val PENDINGINTENT: String get() = FlagVault.reveal("pendingintent")
    val TAPJACKING: String get() = FlagVault.reveal("tapjacking")
    val MEMORY: String get() = FlagVault.reveal("memory")
    val INTENTREDIRECT: String get() = FlagVault.reveal("intentredirect")
    val PATHTRAVERSAL: String get() = FlagVault.reveal("pathtraversal")
    val JWT: String get() = FlagVault.reveal("jwt")
    val STEPUP: String get() = FlagVault.reveal("stepup")
    val TRUSTMANAGER: String get() = FlagVault.reveal("trustmanager")
    val IVREUSE: String get() = FlagVault.reveal("ivreuse")
    val DEXLOAD: String get() = FlagVault.reveal("dexload")
    val SERIALIZATION: String get() = FlagVault.reveal("serialization")
    val NATIVEBRIDGE: String get() = FlagVault.reveal("nativebridge")
    val EMULATOR: String get() = FlagVault.reveal("emulator")
    val REPACKAGING: String get() = FlagVault.reveal("repackaging")
    val TRACKING: String get() = FlagVault.reveal("tracking")
    val NOTIFICATIONS: String get() = FlagVault.reveal("notifications")
    val STRANDHOGG: String get() = FlagVault.reveal("strandhogg")
}
