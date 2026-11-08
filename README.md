<div align="center">

# EvilCorp Android

**An intentionally vulnerable Jetpack Compose laboratory for structured Android security training**

[![CI](https://img.shields.io/github/actions/workflow/status/jankesec/evilcorp-android/ci.yml?branch=main&style=flat-square&label=CI)](https://github.com/jankesec/evilcorp-android/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-8.0%2B-111?style=flat-square&logo=android)](https://developer.android.com/)
[![Challenges](https://img.shields.io/badge/challenges-42-42d77d?style=flat-square)](#challenge-catalog)
[![License](https://img.shields.io/badge/license-MIT-111?style=flat-square)](LICENSE)

Forty-two self-contained labs mapped to CWE, OWASP MASVS v2, MASTG and MASWE, with progressive hints,
CTF flags and scoring, local progress tracking, and adb / Frida / jadx examples per lab.
The Android companion to [evilcorp-ios](https://github.com/jankesec/evilcorp-ios).

</div>

> [!CAUTION]
> EvilCorp is deliberately insecure. Run it only on an emulator or a disposable test
> device. Do not use personal credentials, personal data, production accounts, or a device
> you rely on.

## Interface

<div align="center">
  <img src="screenshots/Home.png" alt="EvilCorp home dashboard" width="19%" />
  <img src="screenshots/Labs.png" alt="Searchable challenge catalog" width="19%" />
  <img src="screenshots/MASVS.png" alt="OWASP MASVS coverage view" width="19%" />
  <img src="screenshots/Progress.png" alt="Training progress dashboard" width="19%" />
  <img src="screenshots/More.png" alt="Operator toolkit" width="19%" />
</div>

## What this project is

Each challenge isolates one recognizable insecure Android pattern and links the hands-on
exercise to industry taxonomy. It is designed for:

- Android application-security workshops and CTFs;
- OWASP MASVS / MASWE / MASTG study;
- static analysis practice (jadx, apktool, manifest review);
- dynamic analysis practice (adb, Frida, objection, drozer, proxies).

It is not a production security reference. Several APIs are used insecurely on purpose
because the insecure behavior is the subject of the lab.

## Quick start

Requirements: Android Studio (or JDK 17+ and the Android SDK with API 36), plus an
emulator or test device running Android 8.0 (API 26) or newer.

```bash
git clone https://github.com/jankesec/evilcorp-android.git
cd evilcorp-android
make run        # build, install on the connected emulator, launch
make check      # build + unit tests (what CI runs)
```

Useful extras: [jadx](https://github.com/skylot/jadx), [apktool](https://apktool.org/),
[Frida](https://frida.re/) / [objection](https://github.com/sensepost/objection).
A Google APIs (non-Play) emulator image works for every lab; Frida needs a rooted
emulator or a frida-gadget build.

## Training workflow

1. Pick a lab from **Labs** or the **MASVS** coverage view.
2. Read the scenario and reproduce the weakness with external tooling.
3. Reveal hints only when stuck (−25 points each, never below half the lab's value).
4. Submit the `EVILCORP{…}` flag.
5. Write down the observation, the affected control, and the remediation shown in the lab.

## Challenge catalog

| # | Challenge | Area | Difficulty | Mapping |
|---:|---|---|---|---|
| 1 | SharedPreferences Storage | Storage | Easy | CWE-312 · STORAGE-1 · MASWE-0006 |
| 2 | SQLite Injection | Storage | Medium | CWE-89 · CODE-4 · MASWE-0079 |
| 3 | Hardcoded Secrets | Storage | Medium | CWE-798 · CRYPTO-2 · MASWE-0005 |
| 4 | Insecure Logging | Storage | Easy | CWE-532 · STORAGE-2 · MASWE-0001 |
| 5 | Broken Cryptography | Crypto | Medium | CWE-327 · CRYPTO-1 · MASWE-0020 |
| 6 | Weak Random Generation | Crypto | Medium | CWE-338 · CRYPTO-1 · MASWE-0027 |
| 7 | WebView JavaScript Bridge | Platform | Medium | CWE-79 · PLATFORM-2 · MASWE-0068 |
| 8 | Exported ContentProvider | Platform | Medium | CWE-926 · PLATFORM-1 · MASWE-0063 |
| 9 | Deep Link Abuse | Platform | Medium | CWE-939 · PLATFORM-1 · MASWE-0058 |
| 10 | Root Detection Bypass | Resilience | Medium | CWE-693 · RESILIENCE-1 · MASWE-0097 |
| 11 | Cleartext HTTP Session | Network | Medium | CWE-319 · NETWORK-1 · MASWE-0050 |
| 12 | External Storage Leak | Storage | Easy | CWE-922 · STORAGE-2 · MASWE-0007 |
| 13 | Unprotected Broadcast Receiver | Platform | Hard | CWE-927 · PLATFORM-1 · MASWE-0064 |
| 14 | Clipboard Leakage | Platform | Easy | CWE-200 · STORAGE-2 · MASWE-0053 |
| 15 | Vulnerable Vault PIN | Auth | Hard | CWE-307 · AUTH-2 · MASWE-0036 |
| 16 | Screenshot Leakage | Platform | Easy | CWE-200 · PLATFORM-3 · MASWE-0055 |
| 17 | PKCS#12 Keystore Misuse | Crypto | Medium | CWE-522 · CRYPTO-2 · MASWE-0013 |
| 18 | Android Backup & Plaintext Files | Storage | Easy | CWE-312 · STORAGE-2 · MASWE-0008 |
| 19 | SSL Pinning Bypass | Network | Hard | CWE-295 · NETWORK-2 · MASWE-0047 |
| 20 | WebView Universal File Access | Platform | Hard | CWE-200 · PLATFORM-2 · MASWE-0070 |
| 21 | Biometric Auth Bypass | Auth | Hard | CWE-287 · AUTH-2 · MASWE-0033 |
| 22 | Debugger & Frida Detection | Resilience | Hard | CWE-693 · RESILIENCE-4 · MASWE-0104 |
| 23 | Keyboard & IME Cache Leak | Storage | Easy | CWE-200 · PLATFORM-3 · MASWE-0054 |
| 24 | Bytecode Tampering & Licensing | Resilience | Hard | CWE-353 · RESILIENCE-2 · MASWE-0100 |
| 25 | HTTP Disk Cache Leak | Network | Medium | CWE-524 · STORAGE-2 · MASWE-0009 |
| 26 | Mutable PendingIntent Hijack | Platform | Hard | CWE-927 · PLATFORM-1 · MASWE-0065 |
| 27 | Tapjacking / Obscured Touch | Platform | Medium | CWE-1021 · PLATFORM-3 · MASWE-0056 |
| 28 | Sensitive Data in Heap Memory | Storage | Hard | CWE-316 · STORAGE-2 · MASWE-0004 · MASTG-TEST-0011 |
| 29 | Intent Redirection | Platform | Hard | CWE-940 · PLATFORM-1 · MASWE-0061 · MASTG-TEST-0027 |
| 30 | Zip Slip / Path Traversal | Code | Hard | CWE-22 · CODE-4 · MASWE-0081 · MASTG-TEST-0043 |
| 31 | Insecure JWT & alg:none | Auth | Medium | CWE-347 · AUTH-1 · MASWE-0031 · MASTG-TEST-0024 |
| 32 | Missing Step-Up Authentication | Auth | Hard | CWE-306 · AUTH-3 · MASWE-0038 · MASTG-TEST-0026 |
| 33 | TrustAll X509TrustManager | Network | Medium | CWE-295 · NETWORK-1 · MASWE-0052 · MASTG-TEST-0021 |
| 34 | Static IV & Nonce Reuse | Crypto | Hard | CWE-329 · CRYPTO-1 · MASWE-0022 · MASTG-TEST-0014 |
| 35 | Unverified Dynamic Code Loading | Code | Hard | CWE-494 · CODE-2 · MASWE-0084 · MASTG-TEST-0041 |
| 36 | Unsafe Java Deserialization | Code | Hard | CWE-502 · CODE-4 · MASWE-0080 · MASTG-TEST-0044 |
| 37 | JNI Stack Buffer Overflow | Code | Hard | CWE-120 · CODE-3 · MASWE-0076 · MASTG-TEST-0042 |
| 38 | Emulator Detection Bypass | Resilience | Medium | CWE-693 · RESILIENCE-1 · MASWE-0099 · MASTG-TEST-0048 |
| 39 | APK Signature & Installer Check | Resilience | Hard | CWE-345 · RESILIENCE-3 · MASWE-0102 · MASTG-TEST-0045 |
| 40 | Persistent Device Fingerprinting | Privacy | Easy | CWE-359 · PRIVACY-3 · MASWE-0110 · MASTG-TEST-0055 |
| 41 | Public Lockscreen Notification Leak | Privacy | Easy | CWE-200 · PRIVACY-2 · MASWE-0057 · MASTG-TEST-0010 |
| 42 | Task Affinity & Activity Hijack | Platform | Hard | CWE-1021 · PLATFORM-3 · MASWE-0062 · MASTG-TEST-0031 |

Mappings are training metadata, not a formal compliance assessment. Confirm current OWASP
definitions before using them in an audit or report.

## Deliberately insecure configuration

- `android:allowBackup="true"`, cleartext traffic allowed, user CAs trusted
  ([network_security_config.xml](app/src/main/res/xml/network_security_config.xml));
- exported `ContentProvider`, `BroadcastReceiver`, deep-link `Activity`, and `IntentRedirectActivity`
  without permissions, plus a non-exported `InternalAdminActivity` reachable via intent redirection;
- mutable implicit `PendingIntent` broadcasts;
- WebViews with JavaScript, a `@JavascriptInterface` bridge, remote debugging, and `file://` access;
- no R8/ProGuard obfuscation on any build type;
- hard-coded synthetic keys, portable PKCS#12 store with default password, static AES-ECB key,
  unsalted MD5 PIN verifier, and immutable `String` secrets retained on the heap;
- client-side authentication, licensing, SSL pinning, anti-debugging, and root-detection decisions.

All network activity stays on the device loopback (`127.0.0.1:8080`, reachable via
`adb forward`). The app makes no external network calls and collects nothing.

## Repository layout

```text
.
├── app/src/main/java/com/evilcorp/android/
│   ├── model/        Challenge catalog (single source of truth) and scoring
│   ├── labs/         42 self-contained security labs + flags + registry
│   ├── data/         Local progress store and runtime security log
│   └── ui/           Compose screens (Home, Labs, MASVS, Progress, More, Detail)
├── app/src/test/     Catalog, scoring and flag invariants
├── Makefile          build / test / install / run / release
└── .github/workflows/ci.yml
```

## Responsible use

Use EvilCorp only on systems you own or are explicitly authorized to test. Read
[CONTRIBUTING.md](CONTRIBUTING.md) before changing lab behavior; repository-level issues
outside the intentionally vulnerable scope follow [SECURITY.md](SECURITY.md).

## License

MIT. See [LICENSE](LICENSE).
