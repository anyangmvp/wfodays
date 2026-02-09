package me.anyang.wfodays

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import me.anyang.wfodays.location.DailyCheckScheduler
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.utils.LocaleHelper
import me.anyang.wfodays.utils.LanguageManager
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class WFOApplication : Application() {

    @Inject
    lateinit var localeHelper: LocaleHelper

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)

        // 确保每日定时任务被调度
        // 使用 REPLACE 策略，如果已经存在则不会重复创建
        DailyCheckScheduler.scheduleDailyCheck(this)
    }

    override fun attachBaseContext(base: Context?) {
        // 在应用启动时应用保存的语言设置
        val context = base?.let {
            val language = LanguageManager.getCurrentLanguage(it)
            LanguageManager.setLanguage(it, language)
            it
        }
        super.attachBaseContext(context)
    }
}
