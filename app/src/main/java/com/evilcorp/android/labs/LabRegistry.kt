package com.evilcorp.android.labs

import androidx.compose.runtime.Composable

/** A lab screen. Its flag is never held here: see [FlagVault]. */
class LabEntry(val content: @Composable () -> Unit)

/** Maps catalog ids to their lab implementation. Flag checks go through [FlagVault]. */
object LabRegistry {
    val entries: Map<String, LabEntry> = mapOf(
        "prefs" to LabEntry { PrefsLab() },
        "sqli" to LabEntry { SqliLab() },
        "secrets" to LabEntry { HardcodedSecretsLab() },
        "logging" to LabEntry { LoggingLab() },
        "crypto" to LabEntry { BrokenCryptoLab() },
        "random" to LabEntry { WeakRandomLab() },
        "webview" to LabEntry { WebViewLab() },
        "provider" to LabEntry { ProviderLab() },
        "deeplink" to LabEntry { DeepLinkLab() },
        "root" to LabEntry { RootLab() },
        "cleartext" to LabEntry { CleartextLab() },
        "external" to LabEntry { ExternalStorageLab() },
        "broadcast" to LabEntry { BroadcastLab() },
        "clipboard" to LabEntry { ClipboardLab() },
        "pin" to LabEntry { PinVaultLab() },
        "screenshot" to LabEntry { ScreenshotLab() },
        "keystore" to LabEntry { KeystoreLab() },
        "backup" to LabEntry { BackupLab() },
        "sslpinning" to LabEntry { SslPinningLab() },
        "webviewfile" to LabEntry { WebViewFileLab() },
        "biometric" to LabEntry { BiometricLab() },
        "antidebug" to LabEntry { AntiDebugLab() },
        "keyboard" to LabEntry { KeyboardCacheLab() },
        "tampering" to LabEntry { TamperingLab() },
        "httpcache" to LabEntry { HttpCacheLab() },
        "pendingintent" to LabEntry { PendingIntentLab() },
        "tapjacking" to LabEntry { TapjackingLab() },
        "memory" to LabEntry { MemoryLab() },
        "intentredirect" to LabEntry { IntentRedirectLab() },
        "pathtraversal" to LabEntry { PathTraversalLab() },
        "jwt" to LabEntry { JwtAuthLab() },
        "stepup" to LabEntry { StepUpAuthLab() },
        "trustmanager" to LabEntry { TrustManagerLab() },
        "ivreuse" to LabEntry { IvReuseLab() },
        "dexload" to LabEntry { DexLoadLab() },
        "serialization" to LabEntry { SerializationLab() },
        "nativebridge" to LabEntry { NativeBridgeLab() },
        "emulator" to LabEntry { EmulatorLab() },
        "repackaging" to LabEntry { RepackagingLab() },
        "tracking" to LabEntry { PrivacyTrackingLab() },
        "notifications" to LabEntry { NotificationLeakLab() },
        "strandhogg" to LabEntry { StrandHoggLab() },
    )

    /** Exact, case-sensitive match against the stored SHA-256 digest (constant-time compare). */
    fun isCorrect(id: String, submitted: String): Boolean =
        id in entries && FlagVault.matches(id, submitted)
}
