package com.dexetra.securekeys

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordGeneratorTest {
    /** golden.tsv: phrase, length, mixedCase, special, numbers, expected - captured from the legacy AAR. */
    @Test
    fun matchesLegacyLibrary() {
        val rows = checkNotNull(javaClass.getResourceAsStream("/golden.tsv"))
            .bufferedReader(Charsets.UTF_8).readLines().filter { it.isNotEmpty() }
        assertEquals(192, rows.size)
        for (row in rows) {
            val (phrase, length, mixed, special, numbers, expected) = row.split('\t')
            assertEquals(
                row,
                expected,
                PasswordGenerator.generate(phrase, length.toInt(), mixed.toBoolean(), special.toBoolean(), numbers.toBoolean()),
            )
        }
    }

    @Test
    fun emptyPassphraseYieldsEmpty() {
        assertEquals("", PasswordGenerator.generate("", 8, true, true, true))
    }

    private operator fun <T> List<T>.component6(): T = this[5]
}
