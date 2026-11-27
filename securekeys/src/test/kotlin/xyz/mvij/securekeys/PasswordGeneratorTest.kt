package xyz.mvij.securekeys

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordGeneratorTest {
    /** golden.tsv: phrase, length, mixedCase, special, numbers, expected - captured from the legacy AAR. */
    @Test
    fun matchesLegacyLibrary() {
        val rows = readRows("/golden.tsv")
        assertEquals(192, rows.size)
        rows.forEach(::assertRow)
    }

    /**
     * golden_extended.tsv: same columns, captured from the legacy AAR by `tools/golden`. Covers 45
     * passphrases (whitespace, case, long input, Unicode, emoji, combining marks) at every length
     * 1-25 with all 8 flag combinations. In the phrase column `\t`, `\n` and `\\` are escaped.
     */
    @Test
    fun matchesLegacyLibraryExtended() {
        val rows = readRows("/golden_extended.tsv")
        assertEquals(45 * 25 * 8, rows.size)
        rows.forEach(::assertRow)
    }

    @Test
    fun emptyPassphraseYieldsEmpty() {
        assertEquals("", PasswordGenerator.generate("", 8, true, true, true))
    }

    @Test
    fun allSpacePassphrasesMatchEachOther() {
        // Spaces are stripped before hashing, so any all-space phrase behaves like the same (empty) seed.
        for (length in 1..25) {
            assertEquals(
                PasswordGenerator.generate(" ", length, true, true, true),
                PasswordGenerator.generate("     ", length, true, true, true),
            )
        }
    }

    private fun readRows(resource: String): List<String> =
        checkNotNull(javaClass.getResourceAsStream(resource)) { "missing $resource" }
            .bufferedReader(Charsets.UTF_8).readLines().filter { it.isNotEmpty() }

    private fun assertRow(row: String) {
        val (phrase, length, mixed, special, numbers, expected) = row.split('\t')
        assertEquals(
            row,
            expected,
            PasswordGenerator.generate(unescape(phrase), length.toInt(), mixed.toBoolean(), special.toBoolean(), numbers.toBoolean()),
        )
    }

    private fun unescape(s: String): String = buildString {
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    't' -> append('\t')
                    'n' -> append('\n')
                    '\\' -> append('\\')
                    else -> error("bad escape in $s")
                }
                i += 2
            } else {
                append(c)
                i++
            }
        }
    }

    private operator fun <T> List<T>.component6(): T = this[5]
}
