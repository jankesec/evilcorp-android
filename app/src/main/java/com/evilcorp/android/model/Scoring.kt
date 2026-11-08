package com.evilcorp.android.model

import kotlin.math.max

/** CTF scoring. Pure Kotlin so it can be unit-tested on the JVM. */
object Scoring {
    const val HINT_PENALTY = 25

    /** Points awarded for solving [challenge] after revealing [hintsUsed] hints (never below 50%). */
    fun award(challenge: Challenge, hintsUsed: Int): Int {
        val base = challenge.difficulty.points
        return max(base / 2, base - HINT_PENALTY * hintsUsed.coerceAtLeast(0))
    }

    fun maxScore(challenges: List<Challenge>): Int = challenges.sumOf { it.difficulty.points }
}
