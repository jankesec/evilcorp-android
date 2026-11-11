# EvilCorp Android — Instructor Walkthrough & SAST/DAST Benchmark Matrix

> [!WARNING]
> This document contains full exploitation commands, solutions, and SAST/DAST coverage expectations for all 42 labs. If you are solving the labs in CTF mode, use the in-app progressive hints before reading this guide.

---

## 1. Quick Exploitation Reference

### Automated Runners

```bash
# Verify every adb-solvable lab (PASS / TODO / FAIL per lab; TODO = press the lab's button first):
./scripts/adb_exploits.sh
./scripts/adb_exploits.sh --reset   # remove the debug.evilcorp.* properties it sets

# Attach universal Frida bypass script (Root, SSL Pinning, Biometrics, Anti-Debug, Licensing, Emulator, Signature):
frida -U -f com.evilcorp.android -l frida/evilcorp_bypass_all.js

# Run Semgrep SAST benchmark rules against the Kotlin codebase:
semgrep --config rules/semgrep-evilcorp.yml app/src/main/
```

### Native Binary Analysis (`libevilcorp_native.so`)

The APK ships unstripped 64-bit ELF libraries (`lib/arm64-v8a/` and `lib/x86_64/`) built from
[`app/src/main/cpp/evilcorp_native.c`](../app/src/main/cpp/evilcorp_native.c). Lab #37's screen
models the JNI frame in Kotlin; the C library is the static-analysis and native-instrumentation
target. It is loaded the first time lab #37 is opened.

```bash
# Symbols and strings:
unzip -o app/build/outputs/apk/debug/app-debug.apk 'lib/*' -d apk-libs
nm -D apk-libs/lib/arm64-v8a/libevilcorp_native.so | grep evilcorp
strings apk-libs/lib/arm64-v8a/libevilcorp_native.so | grep EvilCorp
```

```javascript
// Frida 17 removed the static Module.findExportByName(); resolve exports through the module.
// Open lab #37 first, then attach: frida -U -N com.evilcorp.android -l native.js
const lib = Process.getModuleByName('libevilcorp_native.so');
const verify = new NativeFunction(lib.getExportByName('evilcorp_verify_serial'), 'int', ['pointer', 'int']);
console.log('17-byte serial  ->', verify(Memory.allocUtf8String('EC-NATIVE-9941-OK'), 17));
console.log('20-byte overflow ->', verify(Memory.allocUtf8String('AAAAAAAAAAAAAAAAAAAA'), 20)); // non-zero: is_authenticated overwritten
```

### Flags

Flags are not stored as plaintext in the APK: `app/flags.properties` is compiled into XOR-encoded
bytes and SHA-256 digests at build time, and `./gradlew verifyNoPlaintextFlags` fails the build if a
flag ever shows up in the APK again. A client-side CTF cannot be cheat-proof (reversing `FlagVault`
or hooking it with Frida still reveals everything), so treat flags as learning checkpoints.

---

## 2. SAST vs. DAST / Instrumentation Benchmark Matrix

| # | Challenge | Area | CWE | MASVS v2 | MASWE | Primary Detection Method | Tooling |
|---:|---|---|---|---|---|---|---|
| 01 | SharedPreferences Storage | Storage | CWE-312 | STORAGE-1 | MASWE-0001 | SAST + DAST | `adb`, `jadx`, MobSF |
| 02 | SQLite Injection | Code | CWE-89 | CODE-4 | MASWE-0050 | SAST + DAST | Semgrep, UI input (`' OR 1=1--`) |
| 03 | Hardcoded Secrets | Storage | CWE-798 | STORAGE-1 | MASWE-0004 | SAST | `jadx`, `strings`, trufflehog |
| 04 | Insecure Logging | Storage | CWE-532 | STORAGE-2 | MASWE-0005 | SAST + DAST | `adb logcat`, Semgrep |
| 05 | Broken Cryptography | Crypto | CWE-327 | CRYPTO-1 | MASWE-0007 | SAST | Semgrep, `jadx`, CyberChef |
| 06 | Weak Random Generation | Crypto | CWE-338 | CRYPTO-1 | MASWE-0012 | SAST | Semgrep, `jadx` (`java.util.Random`) |
| 07 | WebView JavaScript Bridge | Platform | CWE-79 | PLATFORM-2 | MASWE-0033 | SAST + DAST | Semgrep, Chrome DevTools, XSS payload |
| 08 | Exported ContentProvider | Platform | CWE-926 | PLATFORM-1 | MASWE-0018 | Manifest SAST + DAST | `adb shell content query`, drozer |
| 09 | Deep Link Abuse | Platform | CWE-939 | PLATFORM-1 | MASWE-0029 | Manifest SAST + DAST | `adb shell am start -d evilcorp://...` |
| 10 | Root Detection Bypass | Resilience | CWE-693 | RESILIENCE-1 | MASWE-0051 | DAST / Instrumentation | Frida (`RootChecker.isRooted`) |
| 11 | Cleartext HTTP Session | Network | CWE-319 | NETWORK-1 | MASWE-0026 | Manifest SAST + DAST | `adb forward`, curl, mitmproxy |
| 12 | External Storage Leak | Storage | CWE-922 | STORAGE-2 | MASWE-0002 | SAST + DAST | `adb shell cat /sdcard/...` |
| 13 | Unprotected Broadcast Receiver | Platform | CWE-925 | PLATFORM-1 | MASWE-0018 | Manifest SAST + DAST | `adb shell am broadcast` |
| 14 | Clipboard Leakage | Platform | CWE-200 | PLATFORM-1 | MASWE-0030 | SAST + DAST | `ClipboardManager` monitor, Frida |
| 15 | Vulnerable Vault PIN | Auth | CWE-307 | AUTH-2 | MASWE-0020 | SAST + DAST | Hashcat / MD5 lookup, Frida |
| 16 | Screenshot Leakage | Platform | CWE-200 | PLATFORM-3 | MASWE-0038 | SAST + DAST | `adb shell screenrecord` (250 ms flash), `FLAG_SECURE` check |
| 17 | PKCS#12 Keystore Misuse | Crypto | CWE-522 | CRYPTO-2 | MASWE-0003 | SAST | `jadx`, `openssl pkcs12` |
| 18 | Android Backup & Plaintext Files | Storage | CWE-312 | STORAGE-2 | MASWE-0006 | Manifest SAST + DAST | `android:allowBackup="true"`, `adb` |
| 19 | SSL Pinning Bypass | Network | CWE-295 | NETWORK-2 | MASWE-0028 | DAST / Instrumentation | Frida (`SslPinValidator`), objection |
| 20 | WebView Universal File Access | Platform | CWE-200 | PLATFORM-2 | MASWE-0034 | SAST + DAST | Semgrep, `file://` XHR payload |
| 21 | Biometric Auth Bypass | Auth | CWE-287 | AUTH-2 | MASWE-0020 | DAST / Instrumentation | Frida (`BiometricGate`), `setprop` |
| 22 | Debugger & Frida Detection | Resilience | CWE-693 | RESILIENCE-4 | MASWE-0064 | DAST / Instrumentation | Frida (`AntiDebugCheck`), `setprop` |
| 23 | Keyboard & IME Cache Leak | Storage | CWE-200 | STORAGE-2 | MASWE-0036 | SAST | `KeyboardOptions` inspection |
| 24 | Bytecode Tampering & Licensing | Resilience | CWE-353 | RESILIENCE-2 | MASWE-0058 | SAST + Instrumentation | `jadx`, Frida (`IntegrityGate`) |
| 25 | HTTP Disk Cache Leak | Storage | CWE-524 | STORAGE-2 | MASWE-0001 | SAST + DAST | `cache/http_cache/` inspection |
| 26 | Mutable PendingIntent Hijack | Platform | CWE-927 | PLATFORM-1 | MASWE-0032 | SAST + DAST | Semgrep (`FLAG_MUTABLE`), receiver intercept |
| 27 | Tapjacking / Obscured Touch | Platform | CWE-1021 | PLATFORM-3 | MASWE-0039 | SAST + DAST | `filterTouchesWhenObscured` audit |
| 28 | Sensitive Data in Heap Memory | Storage | CWE-316 | STORAGE-2 | MASWE-0024 | DAST / Memory Forensics | `am dumpheap`, `strings`, Frida `Java.choose` |
| 29 | Intent Redirection | Platform | CWE-940 | PLATFORM-1 | MASWE-0032 | SAST + DAST | Nested/extra component forwarding via `adb` |
| 30 | Zip Slip / Path Traversal | Code | CWE-22 | CODE-4 | MASWE-0050 | SAST + DAST | Archive entry `../admin_override.flag` with content `UNLOCK` |
| 31 | Insecure JWT & alg:none | Auth | CWE-347 | AUTH-1 | MASWE-0018 | SAST + DAST | Forged `alg:none` token (tampering an HS256 token is rejected) |
| 32 | Missing Step-Up Authentication | Auth | CWE-306 | AUTH-3 | MASWE-0023 | DAST / Logic Audit | Direct sensitive action invocation without MFA |
| 33 | TrustAll X509TrustManager | Network | CWE-295 | NETWORK-1 | MASWE-0027 | SAST + DAST | Empty `checkServerTrusted`, self-signed MITM |
| 34 | Static IV & Nonce Reuse | Crypto | CWE-323 | CRYPTO-1 | MASWE-0007 | SAST + Cryptanalysis | Offline two-time pad `C1 ⊕ P1 ⊕ C2` (the key never leaves the device) |
| 35 | Unverified Dynamic Code Loading | Code | CWE-494 | CODE-4 | MASWE-0049 | SAST + DAST | `jadx` review of `PluginLoader` (simulated loader; the Semgrep rule targets real `DexClassLoader` calls) |
| 36 | Unsafe Java Deserialization | Code | CWE-502 | CODE-4 | MASWE-0050 | SAST + DAST | Semgrep (`ObjectInputStream.readObject`) |
| 37 | JNI Stack Buffer Overflow | Code | CWE-120 | CODE-4 | MASWE-0050 | Native Binary SAST + DAST | Ghidra/IDA (`libevilcorp_native.so`), 17+ byte badge, Frida `NativeFunction` |
| 38 | Emulator Detection Bypass | Resilience | CWE-693 | RESILIENCE-1 | MASWE-0053 | DAST / Instrumentation | Frida (`EmulatorDetector.isEmulator`), QA prop `debug.evilcorp.real_device` |
| 39 | APK Signature & Installer Check | Resilience | CWE-345 | RESILIENCE-2 | MASWE-0056 | SAST + Instrumentation | Frida (`ApkSignatureVerifier`), apktool patch + re-sign |
| 40 | Persistent Device Fingerprinting | Privacy | CWE-359 | PRIVACY-2 | MASWE-0068 | SAST + Privacy Audit | `run-as` read of `files/telemetry_beacon.json` |
| 41 | Public Lockscreen Notification Leak | Platform | CWE-200 | PLATFORM-3 | MASWE-0037 | SAST + DAST | `dumpsys notification --noredact` (token in the extras) |
| 42 | Task Affinity & Activity Hijack | Platform | CWE-1021 | PLATFORM-3 | MASWE-0039 | Manifest SAST + DAST | StrandHogg `taskAffinity` + `allowTaskReparenting` |
