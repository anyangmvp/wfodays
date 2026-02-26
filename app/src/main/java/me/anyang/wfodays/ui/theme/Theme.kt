package me.anyang.wfodays.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 办公专业主题 - 深色模式
private val OfficeDarkColorScheme = darkColorScheme(
    primary = JoyCoral,
    secondary = JoyMint,
    tertiary = JoyPurple,
    background = JoyGray900,
    surface = JoyGray800,
    onPrimary = JoyGray900,
    onSecondary = JoyGray900,
    onTertiary = JoyGray900,
    onBackground = JoyGray100,
    onSurface = JoyGray100,
    error = JoyError,
    onError = JoyGray900
)

// 办公专业主题 - 浅色模式
private val OfficeLightColorScheme = lightColorScheme(
    primary = JoyOrange,
    secondary = JoyMint,
    tertiary = JoyPurple,
    background = JoyBackground,
    surface = JoySurface,
    onPrimary = JoyOnPrimary,
    onSecondary = JoyGray900,
    onTertiary = JoyOnPrimary,
    onBackground = JoyOnBackground,
    onSurface = JoyOnSurface,
    error = JoyError,
    onError = JoyOnPrimary
)

// 向后兼容的别名
private val DarkColorScheme = OfficeDarkColorScheme
private val LightColorScheme = OfficeLightColorScheme

@Composable
fun WFODaysTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
