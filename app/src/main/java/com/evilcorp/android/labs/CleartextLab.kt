package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URL
import kotlin.concurrent.thread

/**
 * Tiny loopback-only HTTP server that plays the "backend". It binds to 127.0.0.1 on the device,
 * so it is reachable only from the device itself or through `adb forward`.
 */
class LabHttpServer(private val port: Int = 8080) {
    @Volatile private var socket: ServerSocket? = null

    fun start() {
        if (socket != null) return
        val s = ServerSocket(port, 8, InetAddress.getByName("127.0.0.1"))
        socket = s
        thread(isDaemon = true, name = "evilcorp-lab-http") {
            while (!s.isClosed) {
                val client = runCatching { s.accept() }.getOrNull() ?: break
                client.use { c ->
                    val reader = BufferedReader(InputStreamReader(c.getInputStream()))
                    val requestLine = reader.readLine() ?: return@use
                    var length = 0
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break
                        if (line.startsWith("Content-Length:", ignoreCase = true)) {
                            length = line.substringAfter(':').trim().toIntOrNull() ?: 0
                        }
                    }
                    val body = CharArray(length).also { if (length > 0) reader.read(it) }.concatToString()
                    val ok = requestLine.startsWith("POST /login") &&
                        body.contains("user=alice") && body.contains("pass=wonderland")
                    val response = if (ok) {
                        "HTTP/1.1 200 OK\r\nSet-Cookie: session=${Flags.CLEARTEXT}; Path=/\r\n" +
                            "Content-Type: application/json\r\nContent-Length: 15\r\nConnection: close\r\n\r\n{\"status\":\"ok\"}"
                    } else {
                        "HTTP/1.1 401 Unauthorized\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                    }
                    c.getOutputStream().write(response.toByteArray())
                }
            }
        }
    }

    fun stop() {
        runCatching { socket?.close() }
        socket = null
    }
}

@Composable
fun CleartextLab() {
    val server = remember { LabHttpServer() }
    val scope = rememberCoroutineScope()
    var log by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val started = runCatching { server.start() }
        log = if (started.isSuccess) "Lab backend listening on http://127.0.0.1:8080 (device loopback)"
        else "Could not start lab backend: ${started.exceptionOrNull()?.message}"
        onDispose { server.stop() }
    }

    LabFrame(
        scenario = "The app logs in to its backend over plain HTTP. Anyone on the path (rogue Wi-Fi, " +
            "proxy, tcpdump) sees the credentials and the session cookie. Capture the session cookie.",
        remediation = "Use HTTPS everywhere; set cleartextTrafficPermitted=\"false\" (the default since " +
            "API 28), don't trust user CAs in release builds, and consider certificate pinning for " +
            "high-value endpoints.",
    ) {
        PrimaryButton("Login as alice") {
            scope.launch {
                val code = withContext(Dispatchers.IO) {
                    runCatching {
                        // INTENTIONALLY INSECURE: credentials over cleartext HTTP
                        val conn = URL("http://127.0.0.1:8080/login").openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.doOutput = true
                        conn.outputStream.use { it.write("user=alice&pass=wonderland".toByteArray()) }
                        conn.responseCode.also { conn.disconnect() }
                    }.getOrElse { -1 }
                }
                log = "POST http://127.0.0.1:8080/login -> HTTP $code\nSession stored (cookie hidden from UI)."
            }
        }
        if (log.isNotEmpty()) Mono(log)
    }
}
