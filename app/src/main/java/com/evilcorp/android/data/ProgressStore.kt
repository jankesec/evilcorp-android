package com.evilcorp.android.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.evilcorp.android.model.Challenge
import com.evilcorp.android.model.Scoring

/** Local-only training progress (solved labs, hints used, bookmarks, CTF mode). */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("evilcorp_progress", Context.MODE_PRIVATE)

    /** challenge id -> awarded points */
    val solved = mutableStateMapOf<String, Int>()

    /** challenge id -> hints revealed */
    val hints = mutableStateMapOf<String, Int>()

    /** challenge id -> bookmarked */
    val bookmarks = mutableStateMapOf<String, Boolean>()

    var ctfMode by mutableStateOf(prefs.getBoolean("ctf_mode", false))
        private set

    var ctfStartMs by mutableLongStateOf(prefs.getLong("ctf_start_ms", 0L))
        private set

    init {
        prefs.all.forEach { (k, v) ->
            when {
                k.startsWith("solved:") && v is Int -> solved[k.removePrefix("solved:")] = v
                k.startsWith("hints:") && v is Int -> hints[k.removePrefix("hints:")] = v
                k.startsWith("bm:") && v is Boolean && v -> bookmarks[k.removePrefix("bm:")] = true
            }
        }
    }

    fun hintsUsed(id: String): Int = hints[id] ?: 0

    fun isSolved(id: String): Boolean = solved.containsKey(id)

    fun revealHint(id: String, max: Int) {
        val next = (hintsUsed(id) + 1).coerceAtMost(max)
        hints[id] = next
        prefs.edit().putInt("hints:$id", next).apply()
        LogStore.append("[HINT] Revealed hint $next for '$id'")
    }

    fun markSolved(challenge: Challenge) {
        if (solved.containsKey(challenge.id)) return
        val points = Scoring.award(challenge, hintsUsed(challenge.id))
        solved[challenge.id] = points
        prefs.edit().putInt("solved:${challenge.id}", points).apply()
        LogStore.append("[SOLVED] ${challenge.title} (+${points} pts)")
    }

    fun toggleSolved(challenge: Challenge) {
        if (solved.containsKey(challenge.id)) {
            solved.remove(challenge.id)
            prefs.edit().remove("solved:${challenge.id}").apply()
            LogStore.append("[PROGRESS] Unmarked ${challenge.title}")
        } else {
            markSolved(challenge)
        }
    }

    fun isBookmarked(id: String): Boolean = bookmarks[id] == true

    fun toggleBookmark(id: String) {
        if (isBookmarked(id)) {
            bookmarks.remove(id)
            prefs.edit().remove("bm:$id").apply()
        } else {
            bookmarks[id] = true
            prefs.edit().putBoolean("bm:$id", true).apply()
        }
    }

    fun startCtf() {
        val now = System.currentTimeMillis()
        ctfMode = true
        ctfStartMs = now
        prefs.edit().putBoolean("ctf_mode", true).putLong("ctf_start_ms", now).apply()
        LogStore.append("[CTF] CTF mode started")
    }

    fun stopCtf() {
        ctfMode = false
        ctfStartMs = 0L
        prefs.edit().putBoolean("ctf_mode", false).remove("ctf_start_ms").apply()
        LogStore.append("[CTF] CTF mode ended (score: ${score()} pts)")
    }

    fun score(): Int = solved.values.sum()

    fun reset() {
        solved.clear()
        hints.clear()
        bookmarks.clear()
        ctfMode = false
        ctfStartMs = 0L
        prefs.edit().clear().apply()
        LogStore.append("[RESET] All progress, bookmarks and CTF state cleared")
    }
}
