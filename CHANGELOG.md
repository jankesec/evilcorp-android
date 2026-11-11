# Changelog

## 1.1.0 (2026-11-11)

Integrity and correctness release. Flag strings are unchanged, so existing progress stays valid.

### CTF integrity
- Flags are no longer plaintext in the APK. Previously `strings classes*.dex | grep EVILCORP{`
  listed all 42. `app/flags.properties` is now compiled at build time into XOR-encoded bytes and
  SHA-256 digests (`FlagVault`), and submissions are checked against the digest in constant time.
- New `verifyNoPlaintextFlags` Gradle task scans the debug APK and fails the build on any leaked
  flag; it runs in `make check`, CI and the release workflow.
- Removed in-app shortcuts that handed out flags without exploiting anything:
  - Serialization: removed the "generate admin payload" helper.
  - IV reuse: the app no longer decrypts for you. It now uses real AES/CTR with a per-process key,
    so the two-time pad is the intended path.
  - Privacy tracking: the app no longer displays the beacon.
  - Emulator: removed the "reported hardware" field and the bypass spelled out in the error.
- Labs that now use the real platform behaviour:
  - Repackaging reads the installed APK's real signing certificate and installer.
  - Notification leak posts a real `VISIBILITY_PUBLIC` notification and carries the token in its
    extras, so you read it with `dumpsys notification`.
  - JWT verifies HS256 signatures but trusts `alg:none`, so the lab now teaches the bug it is
    named after.
  - Screenshot flashes the secret for 250 ms, so the screen has to be captured.
- Descriptions and scenarios no longer contain the answer. Payloads now live only in the
  penalised hints, and a unit test enforces this.

### Taxonomy
- All 42 labs remapped to the stable OWASP MASWE numbering. The previous ids came from the
  retired beta numbering: 32 now pointed to unrelated weaknesses and 10 no longer existed.
  MASTG references point to current Android tests (none where MASTG has no Android test).
- The MASVS list now contains the 24 official v2 controls with their verbatim statements, replacing
  invented ones. This fixes CODE-2/CODE-3, RESILIENCE-3 and PRIVACY-2/3 assignments.
- CWE fixes: #13 → CWE-925, #34 → CWE-323. Lab areas now always match their MASVS family. A unit
  test enforces this.

### Tooling and docs
- `scripts/adb_exploits.sh` rewritten. 5 of its 7 steps targeted names that do not exist in the app
  (prefs file, provider authority, deep-link host, system properties, intent extra). It now checks
  14 labs and reports PASS / TODO / FAIL, with `--reset` to clear the properties it sets.
- Semgrep rules fixed: 5 of 8 matched nothing in the codebase. 5 rules were added; 12 of 13 now
  have true positives.
- Frida suite and native snippets updated for Frida 17 (`Module.findExportByName` was removed).
- Walkthrough matrix regenerated from the catalog. Fixed the zip-slip payload path.
- New `make flags`, `make sast` and `make verify-labs` targets.

### Fixes
- PendingIntent lab no longer crashes on Android 14+ when creating its mutable implicit PendingIntent.
- `versionName` was still `0.1.0`; it is now `1.1.0` (versionCode 2).
- `libevilcorp_native.so` keeps its symbols in the APK on machines with an NDK.

## 1.0.0 (2026-11-09)

- Initial release: 42 labs across all 8 OWASP MASVS v2 categories (Storage, Crypto, Auth, Network, Platform, Code, Resilience, and Privacy).
- Central challenge catalog with CWE, OWASP MASVS v2, MASWE, and MASTG mappings.
- Progressive hints, CTF flag submission and scoring, local progress tracking.
- Built-in Operator Toolkit: Live Security Console, Device Intelligence, and searchable 42-script Frida/ADB/jadx library.
- Standalone `frida/evilcorp_bypass_all.js` and `scripts/adb_exploits.sh` automation scripts.
- Loopback-only HTTP backend for the cleartext lab; no external network calls.
