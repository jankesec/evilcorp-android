package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.GhostButton
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red
import java.io.File

/**
 * INTENTIONALLY INSECURE: loads and executes dynamic plugin descriptors from an unverified writable
 * directory without checking a cryptographic signature or enforcing read-only permissions.
 */
object PluginLoader {
    const val PLUGIN_FILE = "dynamic_plugin.manifest"
    const val EXPLOIT_ENTRY = "com.evilcorp.plugin.DumpVaultPlugin"

    fun writePluginDescriptor(dir: File, entryClass: String): File {
        val f = File(dir, PLUGIN_FILE)
        f.writeText("entry_class=$entryClass\nsignature_verified=false\nsource=/sdcard/Download/plugin.dex\n")
        return f
    }

    @JvmStatic
    fun executeUnverifiedPlugin(dir: File): String {
        val f = File(dir, PLUGIN_FILE)
        if (!f.exists()) return "No plugin staged at ${f.absolutePath}"
        val content = f.readText()
        return if (content.contains("entry_class=$EXPLOIT_ENTRY")) {
            "DexClassLoader loaded unsigned '$EXPLOIT_ENTRY' from writable path!\n" +
                "Executed DumpVaultPlugin.onLoad() -> ${Flags.DEXLOAD}"
        } else {
            "Loaded benign plugin:\n$content\n(Hint: set entry_class to $EXPLOIT_ENTRY)"
        }
    }
}

@Composable
fun DexLoadLab() {
    val context = LocalContext.current
    var entryClass by remember { mutableStateOf("com.evilcorp.plugin.ThemePlugin") }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The plugin engine dynamically loads code via DexClassLoader from a writable " +
            "staging directory without verifying the APK/DEX signature. Replace the plugin entry " +
            "class with '${PluginLoader.EXPLOIT_ENTRY}' and execute the loader.",
        remediation = "Avoid loading executable DEX/JAR/SO code from writable or external storage. " +
            "If dynamic modules are required, use Play Feature Delivery, verify cryptographic " +
            "signatures before loading, and mark DEX files read-only (File.setReadOnly()).",
    ) {
        LabField(entryClass, { entryClass = it }, "DexClassLoader target class name")
        PrimaryButton("Stage & Execute Dynamic Plugin") {
            val dir = context.externalCacheDir ?: context.cacheDir
            PluginLoader.writePluginDescriptor(dir, entryClass.trim())
            val res = PluginLoader.executeUnverifiedPlugin(dir)
            ok = res.contains(Flags.DEXLOAD)
            if (ok) LogStore.append("[CODE] Unsigned DEX plugin executed: $entryClass")
            output = res
        }
        GhostButton("Reset to default plugin") {
            entryClass = "com.evilcorp.plugin.ThemePlugin"
            ok = false
            output = ""
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
