package com.evilcorp.android

import com.evilcorp.android.model.ChallengeCatalog
import com.evilcorp.android.model.Difficulty
import com.evilcorp.android.model.Scoring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {
    private val all = ChallengeCatalog.all

    @Test fun hasFortyTwoChallengesWithUniqueStableIds() {
        assertEquals(42, all.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        all.forEach { assertTrue("bad id ${it.id}", it.id.matches(Regex("[a-z]+"))) }
    }

    @Test fun everyChallengeHasTaxonomyAndHints() {
        all.forEach { c ->
            assertTrue(c.id, c.cwe.matches(Regex("CWE-\\d+")))
            assertTrue(c.id, c.masvs.matches(Regex("[A-Z]+-\\d")))
            // Stable OWASP MASWE ids currently run from MASWE-0001 to MASWE-0078.
            assertTrue(c.id, c.maswe.matches(Regex("MASWE-00[0-7]\\d")))
            // Empty when OWASP MASTG has no Android test for the weakness.
            assertTrue(c.id, c.mastg.isEmpty() || c.mastg.matches(Regex("MASTG-TEST-\\d{4}")))
            assertTrue("missing control info for ${c.masvs}", ChallengeCatalog.controlInfo(c.masvs) != null)
            assertEquals("area/MASVS family mismatch for ${c.id}", c.area.name.uppercase(), c.masvs.substringBefore('-'))
            assertTrue(c.id, c.hints.size >= 3)
            assertTrue(c.id, c.tooling.isNotBlank())
        }
    }

    @Test fun masvsListIsTheOfficialV2ControlSet() {
        val ids = ChallengeCatalog.masvsFamilies.flatMap { f -> f.controls.map { it.id } }
        assertEquals(24, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        ChallengeCatalog.masvsFamilies.forEach { f ->
            f.controls.forEach { assertTrue(it.id, it.id.startsWith(f.code + "-") && it.statement.endsWith(".")) }
        }
    }

    @Test fun descriptionsDoNotGiveAwaySolutions() {
        // Payloads and secrets belong in the (penalised) hints, never in the always-visible description.
        val spoilers = listOf(
            "batch_internal", "DumpVaultPlugin", "target_class", "changeit", "debug.evilcorp", "wonderland",
            "7391", "EC-ENT-2026-PRO", "OR 1=1", "qcom", "EVILCORP{",
        )
        all.forEach { c ->
            spoilers.forEach { s -> assertTrue("${c.id} description leaks '$s'", !c.description.contains(s, ignoreCase = true)) }
        }
    }

    @Test fun scoringNeverDropsBelowHalf() {
        val hard = all.first { it.difficulty == Difficulty.Hard }
        assertEquals(300, Scoring.award(hard, 0))
        assertEquals(250, Scoring.award(hard, 2))
        assertEquals(150, Scoring.award(hard, 99))
        assertEquals(all.sumOf { it.difficulty.points }, Scoring.maxScore(all))
    }
}
