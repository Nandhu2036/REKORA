package com.rekora.app

import androidx.compose.ui.graphics.Color
import com.rekora.app.data.datastore.ThemeMode
import com.rekora.app.ui.theme.RekoraColors
import org.junit.Assert.*
import org.junit.Test

class ThemeModeAndAmoledTest {

    @Test
    fun `test true amoled palette values adhere strictly to pure black directive`() {
        // Directive: Primary dark background must be pure black: #000000
        assertEquals("Primary dark background must be pure black #000000", Color(0xFF000000), RekoraColors.AmoledBackground)
        assertEquals("Secondary background must be #050505", Color(0xFF050505), RekoraColors.AmoledSecondaryBackground)
        assertEquals("Elevated surface must be #101010", Color(0xFF101010), RekoraColors.AmoledSurface)
        assertEquals("Higher-level surface must be #171717", Color(0xFF171717), RekoraColors.AmoledElevatedSurface)
        assertEquals("Primary text must be #F5F5F5", Color(0xFFF5F5F5), RekoraColors.AmoledPrimaryText)
        assertEquals("Secondary text must be #B3B3B3", Color(0xFFB3B3B3), RekoraColors.AmoledSecondaryText)
        assertEquals("Borders and dividers must be #292929", Color(0xFF292929), RekoraColors.AmoledBorder)

        // Compatibility aliases must match Amoled values
        assertEquals(RekoraColors.AmoledBackground, RekoraColors.DarkBackground)
        assertEquals(RekoraColors.AmoledSurface, RekoraColors.DarkSurface)
        assertEquals(RekoraColors.AmoledElevatedSurface, RekoraColors.DarkElevatedSurface)
    }

    @Test
    fun `test theme mode enum supports 3 required operating states`() {
        val modes = ThemeMode.values().map { it.name }
        assertTrue(modes.contains("SYSTEM_DEFAULT"))
        assertTrue(modes.contains("LIGHT"))
        assertTrue(modes.contains("DARK_AMOLED"))
        assertEquals(3, ThemeMode.values().size)
    }
}
