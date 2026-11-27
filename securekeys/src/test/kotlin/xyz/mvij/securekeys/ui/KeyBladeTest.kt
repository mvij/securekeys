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
}
