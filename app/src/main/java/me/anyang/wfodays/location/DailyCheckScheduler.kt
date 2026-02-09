package me.anyang.wfodays.location

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object DailyCheckScheduler {

    private const val WORK_NAME = "daily_location_check"
    private const val RETRY_WORK_NAME = "daily_location_check_retry"

    // 测试模式：每分钟检测一次
    private const val TEST_MODE = true

    fun scheduleDailyCheck(context: Context) {
        val delayMillis = if (TEST_MODE) {
            // 测试模式：15秒后执行
            Duration.ofSeconds(15).toMillis()
        } else {
            val now = LocalDateTime.now()
            var nextCheck = getNextWorkdayCheckTime(now)

            // 计算延迟时间（毫秒）
            Duration.between(now, nextCheck).toMillis()
        }

        // 创建约束配置 - 宽松的约束确保任务能执行
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // 需要网络连接来获取位置
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
        val retryDelayMillis = if (TEST_MODE) {
            // 测试模式：1分钟后重试
            Duration.ofMinutes(1).toMillis()
        } else {
            Duration.ofMinutes(15).toMillis()
        }

        // 重试任务的约束
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
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
