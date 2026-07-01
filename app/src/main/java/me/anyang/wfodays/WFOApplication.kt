package me.anyang.wfodays

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import me.anyang.wfodays.location.DailyCheckScheduler
import me.anyang.wfodays.notification.NotificationHelper

@HiltAndroidApp
class WFOApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        DailyCheckScheduler.scheduleDailyCheck(this)
    }
}
