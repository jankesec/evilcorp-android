package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

/**
 * INTENTIONALLY INSECURE: models a classic JNI C stack frame where an unbounded `strcpy` into a
 * 16-byte `username[16]` buffer overflows into the adjacent `uint8_t is_privileged` field at offset 16.
 */
object NativeBridgeSim {
    const val BUFFER_SIZE = 16

    init {
        runCatching { System.loadLibrary("evilcorp_native") }
    }

    data class NativeFrame(
        val usernameBytes: ByteArray,
        val isPrivilegedByte: Byte,
    ) {
        val corruptedPrivilege: Boolean get() = isPrivilegedByte.toInt() != 0

        fun hexDump(): String {
            val uHex = usernameBytes.joinToString(" ") { "%02x".format(it) }
            val pHex = "%02x".format(isPrivilegedByte)
            return "struct jni_auth_frame {\n" +
                "  char username[16]  = [ $uHex ]\n" +
                "  uint8_t privileged = 0x$pHex (${if (corruptedPrivilege) "OVERWRITTEN != 0" else "0 = unprivileged"})\n" +
                "}"
        }
    }

    @JvmStatic
    fun jniParseBadge(input: String): NativeFrame {
        val raw = input.toByteArray(Charsets.UTF_8)
        val frame = ByteArray(BUFFER_SIZE + 1)
        val copyLen = minOf(raw.size, frame.size)
        for (i in 0 until copyLen) {
            frame[i] = raw[i]
        }
        return NativeFrame(
            usernameBytes = frame.copyOfRange(0, BUFFER_SIZE),
            isPrivilegedByte = frame[BUFFER_SIZE],
        )
    }
}

@Composable
fun NativeBridgeLab() {
    var badgeInput by remember { mutableStateOf("jdoe") }
    var frameDump by remember { mutableStateOf(NativeBridgeSim.jniParseBadge("jdoe").hexDump()) }
    var status by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The native JNI library (libevilcorp_jni.so) copies the badge name into a 16-byte " +
            "stack buffer (char username[16]) right before uint8_t privileged = 0 using strcpy(). " +
            "Overflow past 16 bytes to overwrite the adjacent privilege byte.",
        remediation = "Use bounded functions (snprintf, strlcpy) in C/C++ JNI code, enable compiler " +
            "mitigations (-fstack-protector-strong, _FORTIFY_SOURCE=2, Scudo/MTE), and validate " +
            "input lengths before crossing the JNI boundary.",
    ) {
        LabField(badgeInput, {
            badgeInput = it
            frameDump = NativeBridgeSim.jniParseBadge(it).hexDump()
        }, "JNI badge string (char username[16])")
        Mono(frameDump)
        PrimaryButton("Invoke Java_com_evilcorp_NativeBridge_verifyBadge()") {
            val frame = NativeBridgeSim.jniParseBadge(badgeInput)
            frameDump = frame.hexDump()
            if (frame.corruptedPrivilege) {
                ok = true
                LogStore.append("[JNI] Stack buffer overflow overwrote privileged=0x%02x".format(frame.isPrivilegedByte))
                status = "JNI memory corruption! adjacent `privileged` field != 0\nFlag: ${Flags.NATIVEBRIDGE}"
            } else {
                ok = false
                status = "Standard user badge (${badgeInput.toByteArray().size} bytes <= 16 bytes)."
            }
        }
        if (status.isNotEmpty()) Mono(status, color = if (ok) Green else Red)
    }
}
