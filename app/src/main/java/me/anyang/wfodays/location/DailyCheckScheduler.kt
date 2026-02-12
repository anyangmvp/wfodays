package me.anyang.wfodays.location

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.anyang.wfodays.data.local.PreferencesManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object DailyCheckScheduler {

    private const val WORK_NAME = "daily_location_check"
    private const val RETRY_WORK_NAME = "daily_location_check_retry"

    fun scheduleDailyCheck(context: Context) {
        // 获取调试模式设置
        val (debugMode, intervalMinutes) = runBlocking {
            val preferencesManager = PreferencesManager(context)
            Pair(
                preferencesManager.debugNotificationMode.first(),
                preferencesManager.debugNotificationInterval.first()
            )
        }

        val delayMillis = if (debugMode) {
            // 调试模式：按配置的间隔执行
            val now = LocalDateTime.now()
            val nextCheck = getNextIntervalTime(now, intervalMinutes)
            Duration.between(now, nextCheck).toMillis()
        } else {
            // 正常模式：工作日10:30和16:45执行
            val now = LocalDateTime.now()
            var nextCheck = getNextWorkdayCheckTime(now)
            Duration.between(now, nextCheck).toMillis()
        }

        // 创建约束配置 - 宽松的约束确保任务能执行
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)  // 不强制需要网络，位置检测可以离线进行
            .setRequiresBatteryNotLow(false)  // 允许低电量时运行
            .setRequiresCharging(false)  // 不需要充电
            .setRequiresStorageNotLow(false)  // 不需要存储空间充足
            .build()

        // 创建工作请求
        val workRequest = OneTimeWorkRequestBuilder<DailyLocationCheckWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10_000,  // 10秒初始重试间隔
                TimeUnit.MILLISECONDS
            )
            .addTag("daily_location_check")
            .build()

        // 使用REPLACE策略，确保只有一个定时任务
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // 获取下一个指定间隔的时间（如间隔10分钟：9:00, 9:10, 9:20...）
    private fun getNextIntervalTime(now: LocalDateTime, intervalMinutes: Int): LocalDateTime {
        val currentTime = now.toLocalTime()
        val currentMinute = currentTime.minute
        val nextIntervalMinute = ((currentMinute / intervalMinutes) + 1) * intervalMinutes

        return if (nextIntervalMinute < 60) {
            now.with(LocalTime.of(currentTime.hour, nextIntervalMinute, 0))
        } else {
            // 跨小时，进入下一小时的0分
            now.plusHours(1).with(LocalTime.of((currentTime.hour + 1) % 24, 0, 0))
        }
    }

    // 获取下一个工作日的检查时间（10:30 或 16:00）
    private fun getNextWorkdayCheckTime(now: LocalDateTime): LocalDateTime {
        val currentTime = now.toLocalTime()
        val morningCheck = LocalTime.of(10, 30)  // 上午10:30
        val afternoonCheck = LocalTime.of(16, 0)  // 下午4:00 (16:00)

        // 如果当前时间早于上午检查时间，安排上午检查
        if (currentTime.isBefore(morningCheck)) {
            var nextCheck = now.with(morningCheck)
            // 如果是周末，跳到下一个工作日
            while (nextCheck.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                nextCheck = nextCheck.plusDays(1)
            }
            return nextCheck
        }
        // 如果当前时间在上午检查之后，下午检查之前，安排下午检查
        else if (currentTime.isBefore(afternoonCheck)) {
            var nextCheck = now.with(afternoonCheck)
            // 如果是周末，跳到下一个工作日
            while (nextCheck.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                nextCheck = nextCheck.plusDays(1)
            }
            return nextCheck
        }
        // 如果当前时间已超过下午检查时间，安排到明天上午
        else {
            var nextCheck = now.plusDays(1).with(morningCheck)
            // 跳过周末，找到下一个工作日
            while (nextCheck.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                nextCheck = nextCheck.plusDays(1)
            }
            return nextCheck
        }
    }

    fun scheduleRetry(context: Context) {
        val retryDelayMillis = Duration.ofMinutes(15).toMillis()

        // 重试任务的约束
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)  // 不强制需要网络
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DailyLocationCheckWorker>()
            .setInitialDelay(retryDelayMillis, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10_000,
                TimeUnit.MILLISECONDS
            )
            .addTag("daily_location_retry")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            RETRY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelDailyCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(RETRY_WORK_NAME)
    }
}
