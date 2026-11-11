# Contributing

EvilCorp Android is intentionally vulnerable training software. Contributions should
improve teaching quality, correctness, accessibility, documentation, or reproducibility
without presenting the app as safe for production use.

## Development workflow

1. Use Android Studio (or JDK 17+ and the Android SDK, API 36).
2. Run `make check` before opening a pull request.
3. Keep challenge identifiers stable so existing progress data remains valid.
4. A new lab needs: a catalog entry in `ChallengeCatalog`, a flag in `app/flags.properties` (plus a
   getter in `Flags`), a composable in `labs/`, a `LabRegistry` mapping, and a README row. The unit
   tests enforce the first four.
5. Never write a flag as a string literal in Kotlin or resources: reveal it with `Flags.X` on the
   lab's exploitation path only. `make check` fails if a flag reaches the APK as plaintext.
6. Keep payloads and secrets out of `description` and `scenario`; they belong in the hints.
7. Use stable MASWE ids from [OWASP/maswe](https://github.com/OWASP/maswe), pick a `masvs` control
   from the same MASVS family as the lab's `area`, and leave `mastg` empty when
   [OWASP/mastg](https://github.com/OWASP/mastg) has no Android test for the weakness.

Deliberately vulnerable behavior must stay contained within its named challenge. Do not
add real credentials, production endpoints, third-party data, or undeclared background
activity. Use synthetic values and local-only (loopback) demonstrations.
