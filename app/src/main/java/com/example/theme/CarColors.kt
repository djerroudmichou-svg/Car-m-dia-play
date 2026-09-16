package com.example.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CarColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceSubtle: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val accentBackground: Color,
    val onAccent: Color,
    val controlButtonBg: Color,
    val sliderInactive: Color
) {
    val surfaceSecondary: Color get() = surfaceVariant
    val navBackground: Color get() = if (isDark) Color(0xFF06070E) else Color(0xFFE2E8F0)
}

fun getCarColors(isDark: Boolean, customAccent: Color?): CarColors {
    val accent = customAccent ?: if (isDark) Color(0xFF4DDFBD) else Color(0xFF0E9F6E)

    return if (isDark) {
        CarColors(
            isDark = true,
            background = Color(0xFF070913),
            surface = Color(0xFF0E1222),
            surfaceVariant = Color(0xFF161B30),
            surfaceSubtle = Color(0xFF1D233C),
            cardBorder = Color(0xFF242C4B),
            textPrimary = Color.White,
            textSecondary = Color(0xFF8E9BB0),
            textTertiary = Color(0xFF5E6B83),
            accent = accent,
            accentSecondary = Color(0xFFE5A93C),
            accentBackground = accent.copy(alpha = 0.16f),
            onAccent = Color(0xFF070913),
            controlButtonBg = Color(0xFF1A2038),
            sliderInactive = Color(0xFF222944)
        )
    } else {
        CarColors(
            isDark = false,
            background = Color(0xFFF1F4F9),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE4E9F2),
            surfaceSubtle = Color(0xFFD6DFED),
            cardBorder = Color(0xFFCBD5E1),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textTertiary = Color(0xFF64748B),
            accent = accent,
            accentSecondary = Color(0xFFD97706),
            accentBackground = accent.copy(alpha = 0.14f),
            onAccent = Color.White,
            controlButtonBg = Color(0xFFE2E8F0),
            sliderInactive = Color(0xFFCBD5E1)
        )
    }
}

val LocalCarColors = compositionLocalOf { getCarColors(true, null) }
