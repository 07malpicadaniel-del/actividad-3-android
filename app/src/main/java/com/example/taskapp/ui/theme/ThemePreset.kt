package com.example.taskapp.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemePreset(
    val primaryLight: Color,
    val primaryDark: Color,
    val swatchColor: Color
) {
    MONOCHROME(
        primaryLight = Color(0xFF1C1B1F),
        primaryDark = Color(0xFFFFFFFF),
        swatchColor = Color(0xFF212121)
    ),
    BLUE(
        primaryLight = Color(0xFF1E88E5),
        primaryDark = Color(0xFF90CAF9),
        swatchColor = Color(0xFF1E88E5)
    ),
    PURPLE(
        primaryLight = Color(0xFF8E24AA),
        primaryDark = Color(0xFFCE93D8),
        swatchColor = Color(0xFF8E24AA)
    ),
    EMERALD(
        primaryLight = Color(0xFF2E7D32),
        primaryDark = Color(0xFFA5D6A7),
        swatchColor = Color(0xFF2E7D32)
    ),
    AMBER(
        primaryLight = Color(0xFFFB8C00),
        primaryDark = Color(0xFFFFCC80),
        swatchColor = Color(0xFFFB8C00)
    ),
    PINK(
        primaryLight = Color(0xFFE91E63),
        primaryDark = Color(0xFFF48FB1),
        swatchColor = Color(0xFFE91E63)
    ),
    TEAL(
        primaryLight = Color(0xFF00897B),
        primaryDark = Color(0xFF80CBC4),
        swatchColor = Color(0xFF00897B)
    )
}
