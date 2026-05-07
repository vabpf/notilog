package com.notilog.ui.theme

import androidx.compose.ui.graphics.Color

object Colors {
    // Brand Colors
    val MainBlue = Color(0xFF3B82F6)
    val Teal = Color(0xFF1AC8A8)
    val DeepViolet = Color(0xFF6345C7)
    val Periwinkle = Color(0xFFB9C5E1)

    // Chart Colors
    val Pink = Color(0xFFF472B6)
    val Green = Color(0xFF34D399)
    val Amber = Color(0xFFF59E0B)
    val Purple = Color(0xFF8B5CF6)
    val Indigo = Color(0xFF6366F1)
    val Magenta = Color(0xFFEC4899)
    val Red = Color(0xFFD94A4A)
    val Cyan = Color(0xFF06B6D4)
    val Orange = Color(0xFFF97316)
    val Emerald = Color(0xFF10B981)
    val Slate = Color(0xFF94A3B8)

    // Semantic
    val Error = Red
    val Success = Green
    val Warning = Amber

    // Chart palette list
    val chartColors = listOf(
        MainBlue, Teal, DeepViolet, Periwinkle,
        Pink, Green, Amber, Purple,
        Indigo, Magenta, Red, Cyan,
        Orange, Emerald, Slate
    )
}