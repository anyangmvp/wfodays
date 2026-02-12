package me.anyang.wfodays.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================
// 🌈 快乐活力主题 - Joyful Vibrant Theme
// ========================================
// 设计理念：充满活力、积极向上、温暖愉悦的色彩体验

// 主色调 - 阳光橙到珊瑚粉的渐变
val JoyOrange = Color(0xFFFF6B35)        // 活力橙
val JoyCoral = Color(0xFFFF8E72)         // 珊瑚橙
val JoyPeach = Color(0xFFFFAA8A)         // 蜜桃橙
val JoySunset = Color(0xFFFF7E5F)        // 日落橙

// 辅助色 - 清新薄荷绿
val JoyMint = Color(0xFF2DD4BF)          // 薄荷绿
val JoyMintLight = Color(0xFF5EEAD4)     // 浅薄荷
val JoyMintDark = Color(0xFF14B8A6)      // 深薄荷

// 强调色 - 快乐粉紫
val JoyPink = Color(0xFFF472B6)          // 快乐粉
val JoyPurple = Color(0xFFA78BFA)        // 梦幻紫
val JoyLavender = Color(0xFFC4B5FD)      // 薰衣草紫
val JoyViolet = Color(0xFF8B5CF6)        // 紫罗兰
val JoyTeal = Color(0xFF14B8A6)          // 青色

// 功能色 - 更活泼的语义色
val JoySuccess = Color(0xFF10B981)       // 成功绿
val JoySuccessLight = Color(0xFF34D399)  // 浅成功绿
val JoyWarning = Color(0xFFFBBF24)       // 警告黄
val JoyWarningLight = Color(0xFFFCD34D)  // 浅警告黄
val JoyError = Color(0xFFF87171)         // 错误红
val JoyErrorLight = Color(0xFFFCA5A5)    // 浅错误红
val JoyInfo = Color(0xFF60A5FA)          // 信息蓝

// WFO专用色 - 温暖的办公室氛围
val WFOOrange = Color(0xFFFF6B35)        // WFO主色 - 活力橙
val WFOOrangeLight = Color(0xFFFF8E72)   // WFO浅色
val WFOOrangeDark = Color(0xFFE85A2A)    // WFO深色

// WFH专用色 - 舒适的居家氛围
val WFHMint = Color(0xFF2DD4BF)          // WFH主色 - 薄荷绿
val WFHMintLight = Color(0xFF5EEAD4)     // WFH浅色
val WFHMintDark = Color(0xFF14B8A6)      // WFH深色

// Leave专用色 - 愉悦的假期氛围
val LeaveYellow = Color(0xFFFBBF24)      // Leave主色 - 阳光黄
val LeaveYellowLight = Color(0xFFFCD34D) // Leave浅色
val LeaveYellowDark = Color(0xFFF59E0B)  // Leave深色

// 背景色 - 柔和渐变
val JoyBackground = Color(0xFFFFFBF5)    // 温暖白
val JoyBackgroundLight = Color(0xFFFFF7ED) // 浅橙白
val JoySurface = Color(0xFFFFFFFF)       // 纯白
val JoySurfaceVariant = Color(0xFFFFF1E6) // 浅橙表面

// 文字色
val JoyOnBackground = Color(0xFF1F2937)  // 深灰文字
val JoyOnSurface = Color(0xFF374151)     // 表面文字
val JoyOnSurfaceVariant = Color(0xFF6B7280) // 次要文字
val JoyOnPrimary = Color(0xFFFFFFFF)     // 主色上文字

// 中性色
val JoyGray50 = Color(0xFFFAFAFA)
val JoyGray100 = Color(0xFFF4F4F5)
val JoyGray200 = Color(0xFFE4E4E7)
val JoyGray300 = Color(0xFFD4D4D8)
val JoyGray400 = Color(0xFFA1A1AA)
val JoyGray500 = Color(0xFF71717A)
val JoyGray600 = Color(0xFF52525B)
val JoyGray700 = Color(0xFF3F3F46)
val JoyGray800 = Color(0xFF27272A)
val JoyGray900 = Color(0xFF18181B)

// 渐变色组 - 用于卡片和按钮
val JoyGradientPrimary = listOf(JoyOrange, JoyCoral)
val JoyGradientWFO = listOf(WFOOrange, WFOOrangeLight)
val JoyGradientWFH = listOf(WFHMint, WFHMintLight)
val JoyGradientLeave = listOf(LeaveYellow, LeaveYellowLight)
val JoyGradientSuccess = listOf(JoySuccess, JoySuccessLight)
val JoyGradientSunset = listOf(JoySunset, JoyPeach)
val JoyGradientRainbow = listOf(JoyOrange, JoyPink, JoyPurple, JoyMint)

// 卡片装饰色
val JoyCardAccent1 = Color(0xFFFFE4D6)   // 浅橙装饰
val JoyCardAccent2 = Color(0xFFD1FAE5)   // 浅绿装饰
val JoyCardAccent3 = Color(0xFFFEF3C7)   // 浅黄装饰
val JoyCardAccent4 = Color(0xFFFCE7F3)   // 浅粉装饰

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
