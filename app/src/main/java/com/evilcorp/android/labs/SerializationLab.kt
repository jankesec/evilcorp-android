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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Base64

data class SessionTokenParcel(
    val username: String,
    val isSuperAdmin: Boolean,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1337L
    }
}

/**
 * INTENTIONALLY INSECURE: deserializes untrusted Base64 Java `ObjectInputStream` blobs without
 * an `ObjectInputFilter` or integrity MAC.
 */
object UnsafeDeserializer {
    fun serializeToBase64(token: SessionTokenParcel): String {
        val bos = ByteArrayOutputStream()
        ObjectOutputStream(bos).use { it.writeObject(token) }
        return Base64.getEncoder().encodeToString(bos.toByteArray())
    }

    @JvmStatic
    fun deserializeFromBase64(b64: String): SessionTokenParcel {
        val raw = Base64.getDecoder().decode(b64.trim())
        return ObjectInputStream(ByteArrayInputStream(raw)).use {
            it.readObject() as SessionTokenParcel
        }
    }
}

@Composable
fun SerializationLab() {
    var blob by remember {
        mutableStateOf(UnsafeDeserializer.serializeToBase64(SessionTokenParcel("guest", isSuperAdmin = false)))
    }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The app persists user session state as a raw Java serialized object (rO0AB...) " +
            "and restores it with ObjectInputStream.readObject() without integrity protection. " +
            "Tamper with the serialized stream so isSuperAdmin deserializes as true.",
        remediation = "Do not use Java ObjectInputStream on untrusted data. Prefer structured, " +
            "schema-validated formats (JSON/Protobuf) signed or authenticated on the server, or " +
            "enforce strict ObjectInputFilter allow-lists.",
    ) {
        LabField(blob, { blob = it }, "Base64 Java ObjectInputStream blob")
        GhostButton("Reset to guest stream") {
            blob = UnsafeDeserializer.serializeToBase64(SessionTokenParcel("guest", isSuperAdmin = false))
            ok = false
            output = ""
        }
        PrimaryButton("Deserialize with ObjectInputStream") {
            runCatching {
                val parsed = UnsafeDeserializer.deserializeFromBase64(blob)
                if (parsed.isSuperAdmin) {
                    ok = true
                    LogStore.append("[CODE] Deserialized privileged SessionTokenParcel(user=${parsed.username})")
                    output = "Deserialized: $parsed\nSuperAdmin gadget triggered!\nFlag: ${Flags.SERIALIZATION}"
                } else {
                    ok = false
                    output = "Deserialized: $parsed\nAccess level: standard user."
                }
            }.onFailure {
                ok = false
                output = "StreamCorruptedException / ClassCastException: ${it.message}"
            }
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
