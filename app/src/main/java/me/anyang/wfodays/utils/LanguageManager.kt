package me.anyang.wfodays.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LanguageManager {
    const val LANG_ZH = "zh"
    const val LANG_EN = "en"
    
    /**
     * 设置应用语言并保存到偏好设置
     */
    fun setLanguage(context: Context, languageCode: String) {
        val locale = when (languageCode) {
            LANG_EN -> Locale.ENGLISH
            else -> Locale.SIMPLIFIED_CHINESE
        }
        
        updateLocale(context, locale)
    }
    
    /**
     * 更新 Context 的 locale 配置
     */
    private fun updateLocale(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
        
        // 保存语言设置到偏好设置
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language", locale.language).apply()
    }
    
    /**
     * 获取当前保存的语言设置
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language", LANG_ZH) ?: LANG_ZH
    }
    
    /**
     * 检查当前是否为中文
     */
    fun isChinese(context: Context): Boolean {
        return getCurrentLanguage(context) == LANG_ZH
    }
    
    /**
     * 切换语言
     */
    fun toggleLanguage(context: Context) {
        val currentLang = getCurrentLanguage(context)
        val newLang = if (currentLang == LANG_ZH) LANG_EN else LANG_ZH
        setLanguage(context, newLang)
    }
    
    /**
     * 获取配置好语言的 Context
     * 用于 Worker 和后台任务中获取正确语言的字符串资源
     */
    fun getLocalizedContext(context: Context): Context {
        val language = getCurrentLanguage(context)
        val locale = when (language) {
            LANG_EN -> Locale.ENGLISH
            else -> Locale.SIMPLIFIED_CHINESE
        }
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
    
    /**
     * 安全地重启 Activity 以应用语言更改
     * 避免在 MIUI 设备上使用 recreate() 导致的 ClassCastException
     */
    fun restartActivitySafely(activity: android.app.Activity) {
        if (isMIUI()) {
            // MIUI 设备：使用 FLAG_ACTIVITY_CLEAR_TASK 重启，避免系统 bug
            val intent = android.content.Intent(activity, activity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or 
                          android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            activity.finish()
        } else {
            // 其他设备：使用 recreate() 更平滑
            activity.recreate()
        }
    }
    
    /**
     * 检测是否为 MIUI 系统
     */
    private fun isMIUI(): Boolean {
        return try {
            val properties = java.util.Properties()
            val process = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name")
            properties.load(process.inputStream)
            !properties.getProperty("ro.miui.ui.version.name").isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
}