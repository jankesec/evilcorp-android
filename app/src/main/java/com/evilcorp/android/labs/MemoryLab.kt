package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.ui.GhostButton
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

/**
 * INTENTIONALLY INSECURE: retains sensitive plaintext in an immutable JVM String reference on the
 * heap even after the UI is "locked", so heap dumps (`am dumpheap`) and Frida heap scans find it.
 */
object MemoryVault {
    @JvmField
    var retainedHeapSecret: String? = null

    fun loadSecretIntoHeap() {
        // Construct dynamically so it appears in the live heap once loaded
        retainedHeapSecret = "HEAP_MASTER_KEY=" + Flags.MEMORY
    }
}

@Composable
fun MemoryLab() {
    var screenState by remember { mutableStateOf("Vault idle (secret not loaded into heap yet).") }

    LabFrame(
        scenario = "When you unlock the session and then press \"Lock & Clear Screen\", the UI hides " +
            "the secret, but MemoryVault keeps it in an immutable java.lang.String reference on the " +
            "heap instead of wiping a CharArray/ByteArray. Dump the process memory or inspect the " +
            "MemoryVault singleton to recover the secret.",
        remediation = "Store transient secrets in ByteArray or CharArray, overwrite them with zeros " +
            "(Arrays.fill(buf, 0.toByte())) immediately after use, avoid String concatenation for " +
            "secrets, and keep cryptographic keys inside AndroidKeyStore.",
    ) {
        PrimaryButton("1. Load secret into memory") {
            MemoryVault.loadSecretIntoHeap()
            screenState = "Secret loaded into MemoryVault.retainedHeapSecret (masked on screen: ●●●●●●●●)."
        }
        GhostButton("2. Lock & Clear Screen (leaves String on heap)") {
            screenState = "Screen cleared! UI shows nothing, but MemoryVault.retainedHeapSecret is still alive in the heap."
        }
        Mono(screenState)
    }
}
