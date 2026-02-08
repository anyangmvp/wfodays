package me.anyang.wfodays

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import me.anyang.wfodays.data.local.LanguageManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.utils.LocaleHelper
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class WFOApplication : Application() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var localeHelper: LocaleHelper

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 应用保存的语言设置
        base?.let {
            val prefs = it.getSharedPreferences("language_settings", Context.MODE_PRIVATE)
            val languageCode = prefs.getString("app_language", LanguageManager.LANGUAGE_CHINESE) 
                ?: LanguageManager.LANGUAGE_CHINESE
            val locale = when (languageCode) {
                LanguageManager.LANGUAGE_ENGLISH -> Locale.ENGLISH
                else -> Locale.SIMPLIFIED_CHINESE
            }
            Locale.setDefault(locale)
        }
    }
}
