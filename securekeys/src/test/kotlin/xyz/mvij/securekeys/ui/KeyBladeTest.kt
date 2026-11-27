package xyz.mvij.securekeys.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyBladeTest {
    @Test
    fun bittingHasOneCutPerCharacterWithinRange() {
        val cuts = bitting("Hk3\$Lp0QzR8@vN2bYt6#Wm4x5")
        assertEquals(25, cuts.size)
        assertTrue(cuts.all { it in 0..4 })
    }

    @Test
    fun samePasswordCutsTheSameKey() {
        assertEquals(bitting("f7Kq#2mZp!dX9w"), bitting("f7Kq#2mZp!dX9w"))
    }

    @Test
    fun stampSettlesOnThePasswordAsTheKeyIsCut() {
        val password = "f7Kq#2mZp!dX9w"
        for (step in 0..100) {
            val stamp = stampText(password, step / 100f)
            assertEquals(password.length, stamp.length)
            val settled = (step / 100f * password.length).toInt()
            assertEquals(password.take(settled), stamp.take(settled))
        }
        assertEquals(password, stampText(password, 1f))
    }
}
