# Contributing

EvilCorp Android is intentionally vulnerable training software. Contributions should
improve teaching quality, correctness, accessibility, documentation, or reproducibility
without presenting the app as safe for production use.

## Development workflow

1. Use Android Studio (or JDK 17+ and the Android SDK, API 36).
2. Run `make check` before opening a pull request.
3. Keep challenge identifiers stable so existing progress data remains valid.
4. A new lab needs: a catalog entry in `ChallengeCatalog`, a flag in `Flags`, a composable
   in `labs/`, a `LabRegistry` mapping, and a README row. The unit tests enforce the first four.

Deliberately vulnerable behavior must stay contained within its named challenge. Do not
add real credentials, production endpoints, third-party data, or undeclared background
activity. Use synthetic values and local-only (loopback) demonstrations.
