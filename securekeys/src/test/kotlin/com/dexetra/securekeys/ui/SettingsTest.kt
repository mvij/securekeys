package com.dexetra.securekeys.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {
    @Test
    fun legacyThemeValuesStillMap() {
        // The pre-Kotlin app wrote exactly these strings under current_theme.
        assertEquals(ThemeMode.Dark, ThemeMode.fromPref("DARK"))
        assertEquals(ThemeMode.Light, ThemeMode.fromPref("LIGHT"))
        assertEquals(ThemeMode.System, ThemeMode.fromPref(null))
        assertEquals(ThemeMode.System, ThemeMode.fromPref("SOMETHING_ELSE"))
    }

    @Test
    fun everyModeRoundTripsThroughPrefs() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromPref(it.prefValue)) }
        assertNull(ThemeMode.System.prefValue)
    }

    @Test
    fun onlySystemFollowsTheDevice() {
        assertTrue(ThemeMode.System.isDark(systemDark = true))
        assertFalse(ThemeMode.System.isDark(systemDark = false))
        assertTrue(ThemeMode.Dark.isDark(systemDark = false))
        assertFalse(ThemeMode.Light.isDark(systemDark = true))
    }

    @Test
    fun displayUrlDropsSchemeAndTrailingSlash() {
        assertEquals("github.com/mvij/securekeys", "https://github.com/mvij/securekeys/".displayUrl())
        assertEquals("example.com", "http://example.com".displayUrl())
    }
}
