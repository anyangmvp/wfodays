package me.anyang.wfodays.location

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.data.database.AppDatabase
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

        var result: Result

        try {
            // 获取配置好语言的 Context，确保通知显示正确的语言
            val localizedContext = LanguageManager.getLocalizedContext(applicationContext)
            
            // 创建必要的实例
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = AttendanceRepository(database.attendanceDao(), applicationContext)
            val locationManager = NativeLocationManager(applicationContext)

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
                result = Result.success()
            } else if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                // 检查是否是工作日（周一到周五）
                Log.d(TAG, "[$timeStr] 周末，跳过检测")
                NotificationHelper.showAttendanceNotification(
                    localizedContext,
                    LocalDate.now(),
                    localizedContext.getString(R.string.notification_title_weekend),
                    localizedContext.getString(R.string.notification_message_weekend_rest)
                )
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                result = Result.success()
            } else {
                // 检查今天是否已有WFO记录
                val todayRecord = repository.getTodayRecord()

                // 如果今天已经有WFO记录，不再自动定位和记录
                if (todayRecord != null && (todayRecord.workMode == WorkMode.WFO || todayRecord.workMode == WorkMode.LEAVE)) {
                    Log.d(TAG, "[$timeStr] 今天已有WFO或请假记录，跳过检测")
                    NotificationHelper.showAttendanceNotification(
                        localizedContext,
                        LocalDate.now(),
                        localizedContext.getString(R.string.notification_title_completed),
                        localizedContext.getString(R.string.notification_message_today_completed)
                    )
                    DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                    result = Result.success()
                } else {
                    // 获取当前位置
                    var latitude = 0.0
                    var longitude = 0.0

                    withContext(Dispatchers.Main) {
                        locationManager.getCurrentLocation { lat, lon, _ ->
                            latitude = lat
                            longitude = lon
                        }
                    }
 
                    // 等待位置回调
                    delay(3000)
                        
                    if (latitude != 0.0 && longitude != 0.0) {
                        val distance = locationManager.calculateDistanceToOffice(latitude, longitude)

                        if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                            // 在办公室范围内，记录WFO
                            repository.recordAttendance(
                                date = LocalDate.now(),
                                isPresent = true,
                                workMode = WorkMode.WFO,
                                type = RecordType.AUTO,
                                note = localizedContext.getString(
                                    R.string.location_note_wfo,
                                    NativeLocationManager.OFFICE_NAME,
                                    distance.toInt()
                                )
                            )

                            val distanceDisplay = if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                                val roundedDistance = ((distance / 100).toInt()) * 100
                                localizedContext.getString(R.string.distance_format_string, roundedDistance)
                            } else {
                                localizedContext.getString(R.string.distance_kilometer_format, distance / 1000)
                            }
                    
                            NotificationHelper.showAttendanceNotification(
                                localizedContext,
                                LocalDate.now(),
                                localizedContext.getString(R.string.notification_title_wfo_success),
                                localizedContext.getString(R.string.notification_message_office_distance, distanceDisplay)
                            )

                            Log.d(TAG, "[$timeStr] WFO记录成功，检测完成")
                            // 检测成功，安排明天的检查
                            DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                            result = Result.success()
                        } else {
                            // 不在办公室，记录WFH（只有没有记录时才记录）
                            if (todayRecord == null || todayRecord.workMode != WorkMode.WFO && todayRecord.workMode != WorkMode.LEAVE) {
                                repository.recordAttendance(
                                    date = LocalDate.now(),
                                    isPresent = true,
                                    workMode = WorkMode.WFH,
                                    type = RecordType.AUTO,
                                    note = localizedContext.getString(
                                        R.string.location_note_wfh,
                                        NativeLocationManager.OFFICE_NAME,
                                        distance.toInt()
                                    )
                                )

                                val distanceDisplay = if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                                    val roundedDistance = ((distance / 100).toInt()) * 100
                                    localizedContext.getString(R.string.distance_format_string, roundedDistance)
                                } else {
                                    localizedContext.getString(R.string.distance_kilometer_format, distance / 1000)
                                }
                                
                                NotificationHelper.showAttendanceNotification(
                                    localizedContext,
                                    LocalDate.now(),
                                    localizedContext.getString(R.string.notification_title_wfh_recorded),
                                    localizedContext.getString(R.string.notification_message_home_distance, distanceDisplay)
                                )
                            }

                            Log.d(TAG, "[$timeStr] WFH记录成功，检测完成")
                            // 检测成功，安排明天的检查
                            DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                            result = Result.success()
                        }
                    } else {
                        // 位置获取失败
                        Log.d(TAG, "[$timeStr] 位置获取失败，1分钟后重试")
                        NotificationHelper.showAttendanceNotification(
                            localizedContext,
                            LocalDate.now(),
                            localizedContext.getString(R.string.notification_title_retrying),
                            localizedContext.getString(R.string.notification_message_retrying)
                        )
                        DailyCheckScheduler.scheduleRetry(applicationContext)
                        result = Result.retry()
                    }
                }
            }
        } catch (e: Exception) {
            // 出错时重试
            Log.e(TAG, "[$timeStr] 执行异常: ${e.message}", e)
            NotificationHelper.showAttendanceNotification(
                applicationContext,
                LocalDate.now(),
                applicationContext.getString(R.string.notification_title_system_alert),
                applicationContext.getString(R.string.notification_message_system_issue)
            )
            DailyCheckScheduler.scheduleRetry(applicationContext)
            result = Result.retry()
        }

        return result
    }
}