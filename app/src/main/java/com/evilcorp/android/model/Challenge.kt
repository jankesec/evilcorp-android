package com.evilcorp.android.model

enum class Difficulty(val label: String, val points: Int) {
    Easy("Easy", 100),
    Medium("Medium", 200),
    Hard("Hard", 300),
}

enum class Area(val label: String) {
    Storage("Storage"),
    Crypto("Crypto"),
    Auth("Auth"),
    Network("Network"),
    Platform("Platform"),
    Code("Code"),
    Resilience("Resilience"),
    Privacy("Privacy"),
    ;

    val masvsCategory: String
        get() = "MASVS-${name.uppercase()}"
}

/**
 * Catalog metadata for one lab. The flag itself is NOT stored here: it lives with the lab
 * implementation and is only revealed by exploiting the intended weakness.
 */
data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val area: Area,
    val cwe: String,
    val masvs: String,
    val maswe: String,
    val mastg: String,
    val cvss: String,
    val hints: List<String>,
    /** adb / Frida / shell snippet used as a teaching aid. */
    val tooling: String,
)
