package me.anyang.wfodays.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 重启后重新设置每日定时检查
            DailyCheckScheduler.scheduleDailyCheck(context)
        }
    }
}
