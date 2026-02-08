package me.anyang.wfodays.location

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object DailyCheckScheduler {
    
    private const val WORK_NAME = "daily_location_check"
    
    fun scheduleDailyCheck(context: Context) {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(10, 30) // 10:30 AM
        
        var nextCheck = now.with(targetTime)
        
        // 如果今天10:30已过，则安排到下一天
        if (now.isAfter(nextCheck)) {
            nextCheck = nextCheck.plusDays(1)
        }
        
        // 跳过周末，找到下一个工作日
        while (nextCheck.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            nextCheck = nextCheck.plusDays(1)
        }
        
        // 计算延迟时间（毫秒）
        val delayMillis = Duration.between(now, nextCheck).toMillis()
        
        // 创建工作请求
        val workRequest = OneTimeWorkRequestBuilder<DailyLocationCheckWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()
        
        // 使用REPLACE策略，确保只有一个定时任务
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun cancelDailyCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
