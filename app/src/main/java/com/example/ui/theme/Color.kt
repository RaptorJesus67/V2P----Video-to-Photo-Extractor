package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.content.Context

object ThemeConfig {
    private var _isDarkTheme = mutableStateOf(true)

    var isDarkTheme: Boolean
        get() = _isDarkTheme.value
        set(value) {
            _isDarkTheme.value = value
        }

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", true)
    }

    fun toggleTheme(context: Context) {
        setTheme(context, !isDarkTheme)
    }

    fun setTheme(context: Context, dark: Boolean) {
        _isDarkTheme.value = dark
        val prefs = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_dark_theme", dark).apply()
    }
}

// Beautiful cyber-slate tech color palette, dynamically adjusting to theme state
val TechCyan: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF00E5FF) else Color(0xFF007E8A) // Electric Tech Cyan vs Deep Tech Teal

val CoolGrey: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF78909C) else Color(0xFF546E7A) // Classy Slate Blue Greys

val WarningAmber: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFFF5252) else Color(0xFFD32F2F) // Coral Red vs Deep Crimson

val DarkSlateBg: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF0F1416) else Color(0xFFF1F5F9) // Deep Obsidian Slate vs Clean Studio White

val SlateCard: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF181F22) else Color(0xFFFFFFFF) // High-fidelity dark slate container vs Pure white card

val SlateCardHeader: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF1F282C) else Color(0xFFE2E8F0) // Visual elevation header

val TextLight: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFECEFF1) else Color(0xFF0F172A) // Crisp platinum silver vs Rich deep slate black

val TerminalGreen: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF00E676) else Color(0xFF2E7D32) // Luminous cyber green vs Rich forest green

val TerminalBg: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF111618) else Color(0xFFF1F5F9) // Sleek terminal background matching theme

val BorderSlate: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF263238) else Color(0xFFE2E8F0) // Subtle premium borders

// Additional Brand Accents
val ThumbnailYellow = Color(0xFFFFCC00)  // Brand Golden Yellow from Thumbnail
val ThumbnailRed = Color(0xFFE53935)     // Brand Play Button Red from Thumbnail

val DeleteRed: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFFF3D00) else Color(0xFFC62828) // High saturation electric red vs deep professional red

