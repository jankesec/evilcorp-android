package com.evilcorp.android.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.Scoring

/** Local-only training progress (solved labs, hints used, awarded points). */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("evilcorp_progress", Context.MODE_PRIVATE)

    /** challenge id -> awarded points */
    val solved = mutableStateMapOf<String, Int>()

    /** challenge id -> hints revealed */
    val hints = mutableStateMapOf<String, Int>()

    init {
        prefs.all.forEach { (k, v) ->
            when {
                k.startsWith("solved:") && v is Int -> solved[k.removePrefix("solved:")] = v
                k.startsWith("hints:") && v is Int -> hints[k.removePrefix("hints:")] = v
            }
        }
    }

    fun hintsUsed(id: String): Int = hints[id] ?: 0

    fun revealHint(id: String, max: Int) {
        val next = (hintsUsed(id) + 1).coerceAtMost(max)
        hints[id] = next
        prefs.edit().putInt("hints:$id", next).apply()
    }

    fun markSolved(challenge: Challenge) {
        if (solved.containsKey(challenge.id)) return
        val points = Scoring.award(challenge, hintsUsed(challenge.id))
        solved[challenge.id] = points
        prefs.edit().putInt("solved:${challenge.id}", points).apply()
    }

    fun score(): Int = solved.values.sum()

    fun reset() {
        solved.clear()
        hints.clear()
        prefs.edit().clear().apply()
    }
}
