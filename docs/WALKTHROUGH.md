# EvilCorp Android — Instructor Walkthrough & SAST/DAST Benchmark Matrix

> [!WARNING]
> This document contains full exploitation commands, solutions, and SAST/DAST coverage expectations for all 42 labs. If you are solving the labs in CTF mode, use the in-app progressive hints before reading this guide.

---

## 1. Quick Exploitation Reference

### Automated Runners

```bash
# Run automated ADB exploitation checks across IPC, Storage, and System Properties:
./scripts/adb_exploits.sh

# Attach universal Frida bypass script (Root, SSL Pinning, Biometrics, Anti-Debug, Licensing, Emulator, Signature):
frida -U -f com.evilcorp.android -l frida/evilcorp_bypass_all.js

# Run Semgrep SAST benchmark rules against the Kotlin codebase:
semgrep --config rules/semgrep-evilcorp.yml app/src/main/
```

### Native Binary Analysis (`libevilcorp_native.so`)

The APK ships with unstripped 64-bit ELF shared libraries (`lib/arm64-v8a/libevilcorp_native.so` and `lib/x86_64/libevilcorp_native.so`) compiled from [`app/src/main/cpp/evilcorp_native.c`](../app/src/main/cpp/evilcorp_native.c):

```bash
# Extract strings and symbols from the native library:
strings app/src/main/jniLibs/arm64-v8a/libevilcorp_native.so | grep EvilCorp
nm -g app/src/main/jniLibs/arm64-v8a/libevilcorp_native.so

# Hook native symbol evilcorp_verify_serial in Frida:
frida -U -n com.evilcorp.android -e '
  Interceptor.attach(Module.findExportByName("libevilcorp_native.so", "evilcorp_verify_serial"), {
    onLeave: function (retval) { retval.replace(1); }
  });
'
```

---

## 2. SAST vs. DAST / Instrumentation Benchmark Matrix

| # | Challenge | Area | CWE | MASVS v2 | Primary Detection Method | Tooling |
|---:|---|---|---|---|---|---|
| 01 | SharedPreferences Storage | Storage | CWE-312 | STORAGE-1 | SAST + DAST | `adb`, `jadx`, MobSF |
| 02 | SQLite Injection | Storage | CWE-89 | CODE-4 | SAST + DAST | Semgrep, UI input (`' OR 1=1--`) |
| 03 | Hardcoded Secrets | Storage | CWE-798 | CRYPTO-2 | SAST | `jadx`, `strings`, trufflehog |
| 04 | Insecure Logging | Storage | CWE-532 | STORAGE-2 | SAST + DAST | `adb logcat`, Semgrep |
| 05 | Broken Cryptography | Crypto | CWE-327 | CRYPTO-1 | SAST | Semgrep, `jadx`, CyberChef |
| 06 | Weak Random Generation | Crypto | CWE-338 | CRYPTO-1 | SAST | Semgrep, `jadx` (`java.util.Random`) |
| 07 | WebView JavaScript Bridge | Platform | CWE-79 | PLATFORM-2 | SAST + DAST | Semgrep, Chrome DevTools, XSS payload |
| 08 | Exported ContentProvider | Platform | CWE-926 | PLATFORM-1 | Manifest SAST + DAST | `adb shell content query`, drozer |
| 09 | Deep Link Abuse | Platform | CWE-939 | PLATFORM-1 | Manifest SAST + DAST | `adb shell am start -d evilcorp://...` |
| 10 | Root Detection Bypass | Resilience | CWE-693 | RESILIENCE-1 | DAST / Instrumentation | Frida (`RootChecker.isRooted`) |
| 11 | Cleartext HTTP Session | Network | CWE-319 | NETWORK-1 | Manifest SAST + DAST | `adb forward`, curl, mitmproxy |
| 12 | External Storage Leak | Storage | CWE-922 | STORAGE-2 | SAST + DAST | `adb shell cat /sdcard/...` |
| 13 | Unprotected Broadcast Receiver | Platform | CWE-927 | PLATFORM-1 | Manifest SAST + DAST | `adb shell am broadcast` |
| 14 | Clipboard Leakage | Platform | CWE-200 | STORAGE-2 | SAST + DAST | `ClipboardManager` monitor, Frida |
| 15 | Vulnerable Vault PIN | Auth | CWE-307 | AUTH-2 | SAST + DAST | Hashcat / MD5 lookup, Frida |
| 16 | Screenshot Leakage | Platform | CWE-200 | PLATFORM-3 | SAST + DAST | `FLAG_SECURE` check, `adb screencap` |
| 17 | PKCS#12 Keystore Misuse | Crypto | CWE-522 | CRYPTO-2 | SAST | `jadx`, `openssl pkcs12` |
| 18 | Android Backup & Plaintext Files | Storage | CWE-312 | STORAGE-2 | Manifest SAST + DAST | `android:allowBackup="true"`, `adb` |
| 19 | SSL Pinning Bypass | Network | CWE-295 | NETWORK-2 | DAST / Instrumentation | Frida (`SslPinValidator`), objection |
| 20 | WebView Universal File Access | Platform | CWE-200 | PLATFORM-2 | SAST + DAST | Semgrep, `file://` XHR payload |
| 21 | Biometric Auth Bypass | Auth | CWE-287 | AUTH-2 | DAST / Instrumentation | Frida (`BiometricGate`), `setprop` |
| 22 | Debugger & Frida Detection | Resilience | CWE-693 | RESILIENCE-4 | DAST / Instrumentation | Frida (`AntiDebugCheck`), `setprop` |
| 23 | Keyboard & IME Cache Leak | Storage | CWE-200 | PLATFORM-3 | SAST | `KeyboardOptions` inspection |
| 24 | Bytecode Tampering & Licensing | Resilience | CWE-353 | RESILIENCE-2 | SAST + Instrumentation | `jadx`, Frida (`IntegrityGate`) |
| 25 | HTTP Disk Cache Leak | Network | CWE-524 | STORAGE-2 | SAST + DAST | `cache/http_cache/` inspection |
| 26 | Mutable PendingIntent Hijack | Platform | CWE-927 | PLATFORM-1 | SAST + DAST | Semgrep (`FLAG_MUTABLE`), receiver intercept |
| 27 | Tapjacking / Obscured Touch | Platform | CWE-1021 | PLATFORM-3 | SAST + DAST | `filterTouchesWhenObscured` audit |
| 28 | Sensitive Data in Heap Memory | Storage | CWE-316 | STORAGE-2 | DAST / Memory Forensics | `am dumpheap`, `strings`, Frida `Java.choose` |
| 29 | Intent Redirection | Platform | CWE-940 | PLATFORM-1 | SAST + DAST | Nested/extra component forwarding via `adb` |
| 30 | Zip Slip / Path Traversal | Code | CWE-22 | CODE-4 | SAST + DAST | Archive entry `../../files/pwned.cfg` |
| 31 | Insecure JWT & alg:none | Auth | CWE-347 | AUTH-1 | SAST + DAST | Unverified JWT payload (`alg:none`, `role:admin`) |
| 32 | Missing Step-Up Authentication | Auth | CWE-306 | AUTH-3 | DAST / Logic Audit | Direct sensitive action invocation without MFA |
| 33 | TrustAll X509TrustManager | Network | CWE-295 | NETWORK-1 | SAST + DAST | Empty `checkServerTrusted`, self-signed MITM |
| 34 | Static IV & Nonce Reuse | Crypto | CWE-329 | CRYPTO-1 | SAST + Cryptanalysis | Known-plaintext XOR keystream recovery |
| 35 | Unverified Dynamic Code Loading | Code | CWE-494 | CODE-2 | SAST + DAST | Semgrep (`DexClassLoader`), plugin drop-in |
| 36 | Unsafe Java Deserialization | Code | CWE-502 | CODE-4 | SAST + DAST | Semgrep (`ObjectInputStream.readObject`) |
| 37 | JNI Stack Buffer Overflow | Code | CWE-120 | CODE-3 | Native Binary SAST + DAST | Ghidra/IDA (`libevilcorp_native.so`), 20-byte payload |
| 38 | Emulator Detection Bypass | Resilience | CWE-693 | RESILIENCE-1 | DAST / Instrumentation | Frida (`EmulatorDetector`), `Build` spoofing |
| 39 | APK Signature & Installer Check | Resilience | CWE-345 | RESILIENCE-3 | SAST + Instrumentation | `jadx`, Frida (`ApkSignatureVerifier`) |
| 40 | Persistent Device Fingerprinting | Privacy | CWE-359 | PRIVACY-3 | SAST + Privacy Audit | `ANDROID_ID` / MAC / IMEI telemetry review |
| 41 | Public Lockscreen Notification Leak | Privacy | CWE-200 | PRIVACY-2 | SAST + DAST | `VISIBILITY_PUBLIC` notification inspection |
| 42 | Task Affinity & Activity Hijack | Platform | CWE-1021 | PLATFORM-3 | Manifest SAST + DAST | StrandHogg `taskAffinity` + `allowTaskReparenting` |
