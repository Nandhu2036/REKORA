package com.rekora.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rekora.app.data.datastore.ThemeMode

private val RekoraLightColorScheme = lightColorScheme(
    primary = RekoraColors.MidnightNavy,
    onPrimary = RekoraColors.White,
    primaryContainer = RekoraColors.NavyLight,
    onPrimaryContainer = RekoraColors.MidnightNavy,
    secondary = RekoraColors.EngineeringTeal,
    onSecondary = RekoraColors.White,
    secondaryContainer = RekoraColors.TealLight,
    onSecondaryContainer = RekoraColors.DeepBlue,
    tertiary = RekoraColors.DeepBlue,
    onTertiary = RekoraColors.White,
    background = RekoraColors.LightBackground,
    onBackground = RekoraColors.PrimaryText,
    surface = RekoraColors.White,
    onSurface = RekoraColors.PrimaryText,
    surfaceVariant = Color(0xFFF1F3F5),
    onSurfaceVariant = RekoraColors.SecondaryText,
    outline = RekoraColors.Border,
    outlineVariant = RekoraColors.BorderLight,
    error = RekoraColors.Error,
    onError = RekoraColors.White,
    errorContainer = RekoraColors.ErrorLight,
    onErrorContainer = RekoraColors.Error,
    inverseSurface = RekoraColors.MidnightNavy,
    inverseOnSurface = RekoraColors.White,
    inversePrimary = RekoraColors.AmoledAccent,
    surfaceTint = RekoraColors.EngineeringTeal
)

// True AMOLED Dark Color Scheme — #000000 pure black canvas & neutral surfaces
private val RekoraAmoledColorScheme = darkColorScheme(
    primary = RekoraColors.AmoledAccent,
    onPrimary = RekoraColors.MidnightNavy,
    primaryContainer = Color(0xFF1E3A3A),
    onPrimaryContainer = RekoraColors.AmoledPrimaryText,
    secondary = RekoraColors.AmoledAccent,
    onSecondary = RekoraColors.AmoledBackground,
    secondaryContainer = RekoraColors.AmoledElevatedSurface,
    onSecondaryContainer = RekoraColors.AmoledAccent,
    tertiary = RekoraColors.ElectricCyan,
    onTertiary = RekoraColors.AmoledBackground,
    background = RekoraColors.AmoledBackground, // #000000
    onBackground = RekoraColors.AmoledPrimaryText, // #F5F5F5
    surface = RekoraColors.AmoledSurface, // #101010
    onSurface = RekoraColors.AmoledPrimaryText, // #F5F5F5
    surfaceVariant = RekoraColors.AmoledElevatedSurface, // #171717
    onSurfaceVariant = RekoraColors.AmoledSecondaryText, // #B3B3B3
    outline = RekoraColors.AmoledBorder, // #292929
    outlineVariant = Color(0xFF1C1C1C),
    error = RekoraColors.Error,
    onError = RekoraColors.White,
    errorContainer = Color(0xFF3D1C1C),
    onErrorContainer = RekoraColors.ErrorLight,
    inverseSurface = RekoraColors.White,
    inverseOnSurface = RekoraColors.MidnightNavy,
    inversePrimary = RekoraColors.EngineeringTeal,
    surfaceTint = RekoraColors.AmoledAccent
)

@Composable
fun RekoraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM_DEFAULT,
    dynamicColor: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(), // fallback if legacy caller
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM_DEFAULT -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK_AMOLED -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) {
                // Android 12+ Dynamic Color with strict AMOLED black retention
                dynamicDarkColorScheme(context).copy(
                    background = RekoraColors.AmoledBackground, // Pure #000000
                    surface = RekoraColors.AmoledSurface, // #101010
                    surfaceVariant = RekoraColors.AmoledElevatedSurface, // #171717
                    onBackground = RekoraColors.AmoledPrimaryText, // #F5F5F5
                    onSurface = RekoraColors.AmoledPrimaryText,
                    onSurfaceVariant = RekoraColors.AmoledSecondaryText, // #B3B3B3
                    outline = RekoraColors.AmoledBorder, // #292929
                    outlineVariant = Color(0xFF1C1C1C)
                )
            } else {
                dynamicLightColorScheme(context)
            }
        }
        isDark -> RekoraAmoledColorScheme
        else -> RekoraLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RekoraTypography,
        shapes = RekoraShapes,
        content = content
    )
}
