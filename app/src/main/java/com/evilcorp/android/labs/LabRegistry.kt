package com.evilcorp.android.labs

import androidx.compose.runtime.Composable

class LabEntry(val flag: String, val content: @Composable () -> Unit)

/** Maps catalog ids to their lab implementation and expected flag. */
object LabRegistry {
    val entries: Map<String, LabEntry> = mapOf(
        "prefs" to LabEntry(Flags.PREFS) { PrefsLab() },
        "sqli" to LabEntry(Flags.SQLI) { SqliLab() },
        "secrets" to LabEntry(Flags.SECRETS) { HardcodedSecretsLab() },
        "logging" to LabEntry(Flags.LOGGING) { LoggingLab() },
        "crypto" to LabEntry(Flags.CRYPTO) { BrokenCryptoLab() },
        "random" to LabEntry(Flags.RANDOM) { WeakRandomLab() },
        "webview" to LabEntry(Flags.WEBVIEW) { WebViewLab() },
        "provider" to LabEntry(Flags.PROVIDER) { ProviderLab() },
        "deeplink" to LabEntry(Flags.DEEPLINK) { DeepLinkLab() },
        "root" to LabEntry(Flags.ROOT) { RootLab() },
        "cleartext" to LabEntry(Flags.CLEARTEXT) { CleartextLab() },
        "external" to LabEntry(Flags.EXTERNAL) { ExternalStorageLab() },
        "broadcast" to LabEntry(Flags.BROADCAST) { BroadcastLab() },
        "clipboard" to LabEntry(Flags.CLIPBOARD) { ClipboardLab() },
        "pin" to LabEntry(Flags.PIN) { PinVaultLab() },
        "screenshot" to LabEntry(Flags.SCREENSHOT) { ScreenshotLab() },
        "keystore" to LabEntry(Flags.KEYSTORE) { KeystoreLab() },
        "backup" to LabEntry(Flags.BACKUP) { BackupLab() },
        "sslpinning" to LabEntry(Flags.SSLPINNING) { SslPinningLab() },
        "webviewfile" to LabEntry(Flags.WEBVIEWFILE) { WebViewFileLab() },
        "biometric" to LabEntry(Flags.BIOMETRIC) { BiometricLab() },
        "antidebug" to LabEntry(Flags.ANTIDEBUG) { AntiDebugLab() },
        "keyboard" to LabEntry(Flags.KEYBOARD) { KeyboardCacheLab() },
        "tampering" to LabEntry(Flags.TAMPERING) { TamperingLab() },
        "httpcache" to LabEntry(Flags.HTTPCACHE) { HttpCacheLab() },
        "pendingintent" to LabEntry(Flags.PENDINGINTENT) { PendingIntentLab() },
        "tapjacking" to LabEntry(Flags.TAPJACKING) { TapjackingLab() },
        "memory" to LabEntry(Flags.MEMORY) { MemoryLab() },
        "intentredirect" to LabEntry(Flags.INTENTREDIRECT) { IntentRedirectLab() },
        "pathtraversal" to LabEntry(Flags.PATHTRAVERSAL) { PathTraversalLab() },
    )

    fun isCorrect(id: String, submitted: String): Boolean =
        entries[id]?.flag?.equals(submitted.trim(), ignoreCase = false) == true
}
