package com.dexetra.securekeys

import java.security.MessageDigest
import java.util.Locale

/**
 * Deterministic passphrase -> password derivation.
 *
 * Port of the legacy closed-source SecureKeysLib.aar. Output must stay identical so existing
 * users regenerate the same passwords; `PasswordGeneratorTest` pins it against vectors captured
 * from the original library. Do not "improve" the algorithm without breaking that contract.
 */
object PasswordGenerator {
    const val MIN_LENGTH = 5
    const val MAX_LENGTH = 25

    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "1234567890"
    private const val SYMBOLS = "()-.?[]_~!#$^*-+=|{}"

    fun generate(
        passphrase: String,
        length: Int,
        mixedCase: Boolean,
        special: Boolean,
        numbers: Boolean,
    ): String {
        if (passphrase.isEmpty() || length <= 0) return ""
        val phrase = passphrase.replace(" ", "").lowercase(Locale.ROOT) // legacy: spaces are ignored
        val raw = buildString {
            for (i in 0 until length) append(charAt(phrase, i, mixedCase, special, numbers))
        }
        return rotate(raw, raw[0].code % raw.length)
    }

    private fun charAt(phrase: String, i: Int, mixedCase: Boolean, special: Boolean, numbers: Boolean): Char {
        val seed = md5Hex(rotate(phrase + i, i)).substring(24).toLong(16)
        val position = i + 1
        val pool = when {
            numbers && position % 3 == 0 -> DIGITS
            mixedCase && position % 4 == 0 -> UPPER
            special && position % 5 == 0 -> SYMBOLS
            else -> LOWER
        }
        return pool[(seed % pool.length).toInt()]
    }

    private fun rotate(s: String, n: Int): String {
        var t = s
        while (t.length < n) t += t
        return t.substring(n) + t.substring(0, n)
    }

    private fun md5Hex(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
