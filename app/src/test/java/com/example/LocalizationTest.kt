package com.example

import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class LocalizationTest {

    @Test
    fun testLanguageSelectionRule() {
        // 1. Arabic locale
        Locale.setDefault(Locale("ar", "SA"))
        assertEquals(AppLanguage.ARABIC, LocalizationManager.getSystemDefaultLanguage())

        // 2. French locale
        Locale.setDefault(Locale("fr", "FR"))
        assertEquals(AppLanguage.FRENCH, LocalizationManager.getSystemDefaultLanguage())

        // 3. English locale
        Locale.setDefault(Locale("en", "US"))
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())

        // 4. Any other language strictly defaults to English by law
        Locale.setDefault(Locale("de", "DE")) // German
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())

        Locale.setDefault(Locale("es", "ES")) // Spanish
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())

        Locale.setDefault(Locale("zh", "CN")) // Chinese
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())

        Locale.setDefault(Locale("ru", "RU")) // Russian
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())

        Locale.setDefault(Locale("ja", "JP")) // Japanese
        assertEquals(AppLanguage.ENGLISH, LocalizationManager.getSystemDefaultLanguage())
    }
}
