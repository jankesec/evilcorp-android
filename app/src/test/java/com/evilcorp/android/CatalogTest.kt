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
            assertTrue(c.id, c.maswe.matches(Regex("MASWE-\\d{4}")))
            assertTrue(c.id, c.mastg.matches(Regex("MASTG-TEST-\\d{4}")))
            assertTrue("missing control info for ${c.masvs}", ChallengeCatalog.controlInfo(c.masvs) != null)
            assertTrue(c.id, c.hints.size >= 3)
            assertTrue(c.id, c.tooling.isNotBlank())
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
