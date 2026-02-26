package me.anyang.wfodays.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================
// 办公专业主题 - Office Professional Theme
// ========================================
// 设计理念：专业、稳重、清晰的商务办公色彩体验

// 主色调 - 专业蓝色系
val JoyOrange = Color(0xFF1565C0)        // 主蓝色 - 专业深蓝
val JoyCoral = Color(0xFF42A5F5)         // 亮蓝色 - 辅助
val JoyPeach = Color(0xFF90CAF9)         // 浅蓝色 - 轻量强调
val JoySunset = Color(0xFF1E88E5)        // 中蓝色 - 强调

// 辅助色 - 清新薄荷绿（保持作为辅助）
val JoyMint = Color(0xFF2DD4BF)          // 薄荷绿
val JoyMintLight = Color(0xFF5EEAD4)     // 浅薄荷
val JoyMintDark = Color(0xFF14B8A6)      // 深薄荷

// 强调色 - 紫色调
val JoyPink = Color(0xFF7E57C2)          // 紫罗兰
val JoyPurple = Color(0xFF5C6BC0)        // 靛蓝紫
val JoyLavender = Color(0xFF9FA8DA)      // 薰衣草
val JoyViolet = Color(0xFF3949AB)        // 深紫
val JoyTeal = Color(0xFF00897B)          // 青色

// 功能色 - 专业语义色
val JoySuccess = Color(0xFF43A047)       // 成功绿
val JoySuccessLight = Color(0xFF66BB6A)  // 浅成功绿
val JoyWarning = Color(0xFFFFA726)       // 警告橙
val JoyWarningLight = Color(0xFFFFB74D)  // 浅警告橙
val JoyError = Color(0xFFE53935)         // 错误红
val JoyErrorLight = Color(0xFFEF5350)    // 浅错误红
val JoyInfo = Color(0xFF1E88E5)          // 信息蓝

// WFO专用色 - 办公蓝色
val WFOOrange = Color(0xFF1565C0)        // WFO主色 - 深蓝
val WFOOrangeLight = Color(0xFF42A5F5)   // WFO浅色
val WFOOrangeDark = Color(0xFF0D47A1)    // WFO深色

// WFH专用色 - 舒适的居家蓝绿色
val WFHMint = Color(0xFF00897B)          // WFH主色 -  teal
val WFHMintLight = Color(0xFF26A69A)     // WFH浅色
val WFHMintDark = Color(0xFF00695C)      // WFH深色

// Leave专用色 - 愉悦的假期琥珀色
val LeaveYellow = Color(0xFFFFA726)      // Leave主色 - 琥珀
val LeaveYellowLight = Color(0xFFFFB74D) // Leave浅色
val LeaveYellowDark = Color(0xFFF57C00)  // Leave深色

// 背景色 - 专业灰白
val JoyBackground = Color(0xFFF5F7FA)    // 浅灰白背景
val JoyBackgroundLight = Color(0xFFFAFBFC) // 更浅背景
val JoySurface = Color(0xFFFFFFFF)       // 纯白表面
val JoySurfaceVariant = Color(0xFFECEFF1) // 浅灰表面

// 文字色
val JoyOnBackground = Color(0xFF263238)  // 深色文字
val JoyOnSurface = Color(0xFF37474F)     // 表面文字
val JoyOnSurfaceVariant = Color(0xFF607D8B) // 次要文字
val JoyOnPrimary = Color(0xFFFFFFFF)     // 主色上文字

// 中性色
val JoyGray50 = Color(0xFFFAFAFA)
val JoyGray100 = Color(0xFFF5F5F5)
val JoyGray200 = Color(0xFFEEEEEE)
val JoyGray300 = Color(0xFFE0E0E0)
val JoyGray400 = Color(0xFFBDBDBD)
val JoyGray500 = Color(0xFF9E9E9E)
val JoyGray600 = Color(0xFF757575)
val JoyGray700 = Color(0xFF616161)
val JoyGray800 = Color(0xFF424242)
val JoyGray900 = Color(0xFF212121)

// 渐变色组 - 用于卡片和按钮
val JoyGradientPrimary = listOf(JoyOrange, JoyCoral)
val JoyGradientWFO = listOf(WFOOrange, WFOOrangeLight)
val JoyGradientWFH = listOf(WFHMint, WFHMintLight)
val JoyGradientLeave = listOf(LeaveYellow, LeaveYellowLight)
val JoyGradientSuccess = listOf(JoySuccess, JoySuccessLight)
val JoyGradientSunset = listOf(JoySunset, JoyPeach)
val JoyGradientRainbow = listOf(JoyOrange, JoyPink, JoyPurple, JoyMint)

// 卡片装饰色
val JoyCardAccent1 = Color(0xFFE3F2FD)   // 浅蓝装饰
val JoyCardAccent2 = Color(0xFFE8F5E9)   // 浅绿装饰
val JoyCardAccent3 = Color(0xFFFFF3E0)   // 浅琥珀装饰
val JoyCardAccent4 = Color(0xFFF3E5F5)   // 浅紫装饰

// 阴影色
val JoyShadowOrange = JoyOrange.copy(alpha = 0.25f)
val JoyShadowMint = JoyMint.copy(alpha = 0.25f)
val JoyShadowYellow = LeaveYellow.copy(alpha = 0.25f)

// ========================================
// 向后兼容 - 保留旧的颜色别名
// ========================================
val PrimaryBlue = JoyOrange
val PrimaryBlueDark = WFOOrangeDark
val PrimaryBlueLight = JoyCoral
val SecondaryBlue = JoyMint
val AccentBlue = JoyLavender

val SuccessGreen = JoySuccess
val WarningYellow = JoyWarning
val ErrorRed = JoyError
val InfoBlue = JoyInfo

val BackgroundLight = JoyBackground
val SurfaceLight = JoySurface
val OnSurfaceLight = JoyOnSurface
val OnSurfaceVariantLight = JoyOnSurfaceVariant

val BackgroundDark = JoyGray900
val SurfaceDark = JoyGray800
val OnSurfaceDark = Color(0xFFF4F4F5)
val OnSurfaceVariantDark = JoyGray400

val CardBackgroundLight = JoySurface
val CardBackgroundDark = JoyGray800

val DividerLight = JoyGray200
val DividerDark = JoyGray700

val ProgressStart = JoyOrange
val ProgressEnd = JoyCoral

val NeutralWhite = Color.White
val NeutralGray50 = JoyGray50
val NeutralGray100 = JoyGray100
val NeutralGray200 = JoyGray200
val NeutralGray300 = JoyGray300
val NeutralGray400 = JoyGray400
val NeutralGray500 = JoyGray500
val NeutralGray600 = JoyGray600
val NeutralGray700 = JoyGray700
val NeutralGray800 = JoyGray800
val NeutralGray900 = JoyGray900

// Legacy aliases
val HSBCRed = JoyOrange
val HSBCRedDark = WFOOrangeDark
val HSBCRedLight = JoyCoral
val HSBCWhite = Color.White
val HSBCGray = JoyGray100
val HSBCGrayDark = JoyGray800
val HSBCGrayMedium = JoyGray500
