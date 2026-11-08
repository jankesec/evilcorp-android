package com.evilcorp.android

import com.evilcorp.android.labs.LabRegistry
import com.evilcorp.android.labs.PinVault
import com.evilcorp.android.labs.WeakRandom
import com.evilcorp.android.model.ChallengeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabRegistryTest {
    @Test fun everyCatalogEntryHasALabAndViceVersa() {
        assertEquals(ChallengeCatalog.all.map { it.id }.toSet(), LabRegistry.entries.keys)
    }

    @Test fun flagsAreUniqueAndWellFormed() {
        val flags = LabRegistry.entries.values.map { it.flag }
        assertEquals(flags.size, flags.toSet().size)
        flags.forEach { assertTrue(it, it.matches(Regex("EVILCORP\\{[A-Za-z0-9_]+}"))) }
    }

    @Test fun flagCheckIsExact() {
        val f = LabRegistry.entries.getValue("sqli").flag
        assertTrue(LabRegistry.isCorrect("sqli", "  $f "))
        assertFalse(LabRegistry.isCorrect("sqli", f.lowercase()))
        assertFalse(LabRegistry.isCorrect("prefs", f))
    }

    @Test fun pinLabIsSolvableByBruteForce() {
        val pin = (0..9999).map { "%04d".format(it) }.first(PinVault::check)
        assertEquals("7391", pin)
    }

    @Test fun weakRandomIsDeterministic() {
        assertEquals(WeakRandom.resetToken(1_700_000_000L), WeakRandom.resetToken(1_700_000_000L))
    }
}
