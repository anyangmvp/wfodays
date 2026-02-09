package me.anyang.wfodays.location

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.anyang.wfodays.R
import me.anyang.wfodays.data.database.AppDatabase
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.utils.LanguageManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DailyLocationCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DailyLocationCheck"
        // 工作时间范围：早上9点到下午6点半
        private val WORK_START_TIME = LocalTime.of(9, 0)
        private val WORK_END_TIME = LocalTime.of(18, 30)
    }

    override suspend fun doWork(): Result {
        val now = LocalDateTime.now()
        val timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        Log.d(TAG, "[$timeStr] Worker 被系统触发执行！")
        Log.d(TAG, "[$timeStr] 定时任务开始执行")

        // 获取配置好语言的 Context，确保通知显示正确的语言
        val localizedContext = LanguageManager.getLocalizedContext(applicationContext)

        return try {
            // 创建必要的实例
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = AttendanceRepository(database.attendanceDao(), applicationContext)
            val locationManager = NativeLocationManager(applicationContext)
            val locationRecorder = LocationBasedAttendanceRecorder(localizedContext, repository, locationManager)

            val currentTime = now.toLocalTime()
            val today = now.toLocalDate()

            // 检查当前时间是否在工作时间范围内（9:00 - 18:30）
            if (currentTime.isBefore(WORK_START_TIME) || currentTime.isAfter(WORK_END_TIME)) {
                Log.d(TAG, "[$timeStr] 不在工作时间范围内，跳过检测")
                NotificationHelper.showAttendanceNotification(
                    localizedContext,
                    LocalDate.now(),
                    localizedContext.getString(R.string.notification_title_overtime),
                    localizedContext.getString(R.string.notification_message_outside_working_hours)
                )
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

            // 检查是否是工作日（周一到周五）
            if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                Log.d(TAG, "[$timeStr] 周末，跳过检测")
                NotificationHelper.showAttendanceNotification(
                    localizedContext,
                    LocalDate.now(),
                    localizedContext.getString(R.string.notification_title_weekend),
                    localizedContext.getString(R.string.notification_message_weekend_rest)
                )
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

            // 检查今天是否已有WFO或请假记录
            val todayRecord = repository.getTodayRecord()
            if (todayRecord != null && (todayRecord.workMode == WorkMode.WFO || todayRecord.workMode == WorkMode.LEAVE)) {
                Log.d(TAG, "[$timeStr] 今天已有WFO或请假记录，跳过检测")
                NotificationHelper.showAttendanceNotification(
                    localizedContext,
                    LocalDate.now(),
                    localizedContext.getString(R.string.notification_title_completed),
                    localizedContext.getString(R.string.notification_message_today_completed)
                )
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

            // 执行位置检测并记录
            val result = locationRecorder.detectAndRecord(
                skipExistingCheck = false,
                showNotification = false
            )

            when (result) {
                is LocationBasedAttendanceRecorder.RecordResult.Success -> {
                    Log.d(TAG, "[$timeStr] ${result.workMode}记录成功，检测完成")
                    DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                    Result.success()
                }
                is LocationBasedAttendanceRecorder.RecordResult.Skipped -> {
                    Log.d(TAG, "[$timeStr] 跳过记录: ${result.reason}")
                    // 位置获取失败时显示重试通知
                    if (result.reason == "位置获取失败") {
                        NotificationHelper.showAttendanceNotification(
                            localizedContext,
                            LocalDate.now(),
                            localizedContext.getString(R.string.notification_title_retrying),
                            localizedContext.getString(R.string.notification_message_retrying)
                        )
                    }
                    DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                    Result.success()
                }
                is LocationBasedAttendanceRecorder.RecordResult.Error -> {
                    Log.e(TAG, "[$timeStr] 记录失败: ${result.exception.message}")
                    NotificationHelper.showAttendanceNotification(
                        localizedContext,
                        LocalDate.now(),
                        localizedContext.getString(R.string.notification_title_system_alert),
                        localizedContext.getString(R.string.notification_message_system_issue)
                    )
                    DailyCheckScheduler.scheduleRetry(applicationContext)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[$timeStr] 执行异常: ${e.message}", e)
            NotificationHelper.showAttendanceNotification(
                localizedContext,
                LocalDate.now(),
                localizedContext.getString(R.string.notification_title_system_alert),
                localizedContext.getString(R.string.notification_message_system_issue)
            )
            DailyCheckScheduler.scheduleRetry(applicationContext)
            Result.retry()
        }
    }
}
