package me.anyang.wfodays.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================
// WFO Days - Clean iOS-style Theme
// ========================================

// Primary Colors - Bright Blue
val PrimaryBlue = Color(0xFF2563EB)        // Main blue
val PrimaryBlueDark = Color(0xFF1D4ED8)    // Darker blue
val PrimaryBlueLight = Color(0xFF60A5FA)   // Lighter blue

// Status Colors - Matching target design
val WFOBlue = Color(0xFF2563EB)            // WFO - Blue
val WFOBlueLight = Color(0xFF93C5FD)       // WFO light
val WFHGreen = Color(0xFF22C55E)           // WFH - Green
val WFHGreenLight = Color(0xFF86EFAC)      // WFH light
val LeaveOrange = Color(0xFFF97316)        // Leave - Orange
val LeaveOrangeLight = Color(0xFFFDBA74)   // Leave light

// Semantic Colors
val SuccessGreen = Color(0xFF22C55E)
val SuccessGreenLight = Color(0xFF86EFAC)
val WarningOrange = Color(0xFFF97316)
val WarningOrangeLight = Color(0xFFFDBA74)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF2563EB)

// Background Colors
val BackgroundWhite = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF8FAFC)    // Very light gray
val BackgroundCard = Color(0xFFFFFFFF)     // White cards

// Surface Colors
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFF1F5F9)       // Light surface
val SurfaceVariant = Color(0xFFE2E8F0)     // Variant surface

// Text Colors
val TextPrimary = Color(0xFF1E293B)        // Dark text
val TextSecondary = Color(0xFF64748B)      // Gray text
val TextTertiary = Color(0xFF94A3B8)       // Lighter gray
val TextOnPrimary = Color(0xFFFFFFFF)      // White on primary
val TextOnDark = Color(0xFFFFFFFF)         // White on dark

// Neutral Gray Scale (Cool tones)
val Gray50 = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)
val Gray200 = Color(0xFFE2E8F0)
val Gray300 = Color(0xFFCBD5E1)
val Gray400 = Color(0xFF94A3B8)
val Gray500 = Color(0xFF64748B)
val Gray600 = Color(0xFF475569)
val Gray700 = Color(0xFF334155)
val Gray800 = Color(0xFF1E293B)
val Gray900 = Color(0xFF0F172A)

// Divider
val Divider = Color(0xFFE2E8F0)

// Chart Colors
val ChartBlue = Color(0xFF2563EB)
val ChartGreen = Color(0xFF22C55E)
val ChartOrange = Color(0xFFF97316)
val ChartGray = Color(0xFFCBD5E1)

// ========================================
// Legacy aliases for backward compatibility
// ========================================
val JoyOrange = PrimaryBlue
val JoyCoral = PrimaryBlueLight
val JoyMint = WFHGreen
val JoySuccess = SuccessGreen
val JoyWarning = WarningOrange
val JoyError = ErrorRed

val WFOOrange = WFOBlue
val WFOOrangeLight = WFOBlueLight
val WFOOrangeDark = PrimaryBlueDark

val WFHMint = WFHGreen
val WFHMintLight = WFHGreenLight

val LeaveYellow = LeaveOrange
val LeaveYellowLight = LeaveOrangeLight

val JoyBackground = BackgroundLight
val JoySurface = SurfaceWhite
val JoyOnBackground = TextPrimary
val JoyOnSurface = TextPrimary
val JoyOnSurfaceVariant = TextSecondary
val JoyOnPrimary = TextOnPrimary

val JoyGray50 = Gray50
val JoyGray100 = Gray100
val JoyGray200 = Gray200
val JoyGray300 = Gray300
val JoyGray400 = Gray400
val JoyGray500 = Gray500
val JoyGray600 = Gray600
val JoyGray700 = Gray700
val JoyGray800 = Gray800
val JoyGray900 = Gray900

val WarningYellow = WarningOrange

// Legacy HSBC aliases
val HSBCRed = PrimaryBlue
val HSBCRedDark = PrimaryBlueDark
val HSBCRedLight = PrimaryBlueLight
val HSBCWhite = Color.White
val HSBCGray = Gray100
val HSBCGrayDark = Gray800
val HSBCGrayMedium = Gray500

val NeutralGray200 = Gray200
val NeutralGray400 = Gray400
val NeutralGray500 = Gray500
val NeutralGray600 = Gray600
val NeutralGray700 = Gray700
val NeutralGray900 = Gray900
