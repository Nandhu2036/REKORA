package com.rekora.app.ui.theme

import androidx.compose.ui.graphics.Color

// REKORA Brand Palette — Apple Music × Google Play Store × True AMOLED Engineering Innovation
object RekoraColors {
    // Primary Authority
    val MidnightNavy = Color(0xFF0B1B2B)
    val DeepBlue = Color(0xFF173D5B)
    val SignatureTeal = Color(0xFF2B8992)
    val EngineeringTeal = Color(0xFF2B8992) // Keep alias for backward compatibility

    // Neutrals & Surfaces (Light Theme)
    val WarmWhite = Color(0xFFFAFAF8)
    val LightBackground = Color(0xFFFAFAF8) // Primary light background
    val PureWhite = Color(0xFFFFFFFF)
    val White = Color(0xFFFFFFFF)
    val LightElevatedSurface = Color(0xFFFFFFFF)
    val Graphite = Color(0xFF252A30)
    val PrimaryText = Color(0xFF252A30) // Graphite for authority
    val SecondaryText = Color(0xFF727A84) // Secondary typography
    val TertiaryText = Color(0xFF9CA3AF)
    val AppleSubtleGray = Color(0xFF86868B)

    // Fine Borders & Separators
    val FineBorder = Color(0xFFE5E8EB)
    val Border = Color(0xFFE5E8EB)
    val BorderLight = Color(0xFFF0F2F4)
    val Hairline = Color(0x120B1B2B) // Restrained 7% hairline stroke
    val DarkHairline = Color(0x2E292929)

    // Accents & Status (Used with engineering restraint)
    val ElectricCyan = Color(0xFF00E5D0)
    val CyberIndigo = Color(0xFF4F46E5)
    val Success = Color(0xFF1E824C)
    val SuccessLight = Color(0xFFEAF8F0)
    val SuccessBorder = Color(0xFFA3E4C1)

    val Warning = Color(0xFFD97706)
    val WarningLight = Color(0xFFFFFBEB)
    val WarningBorder = Color(0xFFFDE68A)

    val Error = Color(0xFFDC2626)
    val ErrorLight = Color(0xFFFEF2F2)
    val ErrorBorder = Color(0xFFFECACA)

    // True AMOLED Dark Theme — Pure Black Canvas & Neutral Tonal Hierarchy
    val AmoledBackground = Color(0xFF000000) // Pure Black (#000000)
    val AmoledSecondaryBackground = Color(0xFF050505) // (#050505)
    val AmoledSurface = Color(0xFF101010) // Elevated surfaces (#101010)
    val AmoledElevatedSurface = Color(0xFF171717) // Higher-level surfaces (#171717)
    val AmoledCardSurface = Color(0xFF101010)
    val AmoledPrimaryText = Color(0xFFF5F5F5) // Primary text (#F5F5F5)
    val AmoledSecondaryText = Color(0xFFB3B3B3) // Secondary text (#B3B3B3)
    val AmoledBorder = Color(0xFF292929) // Borders & dividers (#292929)
    val AmoledAccent = Color(0xFF5EEAD4)

    // Compatibility aliases mapping directly to True AMOLED palette
    val DarkBackground = AmoledBackground
    val DarkSurface = AmoledSurface
    val DarkElevatedSurface = AmoledElevatedSurface
    val DarkCardSurface = AmoledCardSurface
    val DarkPrimaryText = AmoledPrimaryText
    val DarkSecondaryText = AmoledSecondaryText
    val DarkAccent = AmoledAccent

    // Frosted Glass & Scrims
    val GlassWhite = Color(0xF7FAFAF8)
    val GlassWhiteSubtle = Color(0xE6FFFFFF)
    val GlassNavy = Color(0xEE0B1B2B)
    val DarkGlassSurface = Color(0xEE101010)
    val Overlay = Color(0x66000000)
    val Scrim = Color(0x33000000)

    // Schematic & Trace Tints
    val BlueprintGrid = Color(0x122B8992)
    val BlueprintGridBright = Color(0x2E2B8992)
    val CircuitTrace = Color(0x3D2B8992)
    val CircuitVia = Color(0x662B8992)

    // Supplementary
    val TealLight = Color(0xFFEBF6F7)
    val TealSubtle = Color(0xFFF3FAF9)
    val NavyLight = Color(0xFFE8EDF2)
    val Shimmer = Color(0xFFEFF1F4)
    val ShimmerHighlight = Color(0xFFF8F9FA)
    val DarkShimmer = Color(0xFF171717)
    val DarkShimmerHighlight = Color(0xFF222222)
}
