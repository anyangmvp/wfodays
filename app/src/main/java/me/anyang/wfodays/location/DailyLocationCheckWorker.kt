package me.anyang.wfodays.location

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DailyLocationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationManager: NativeLocationManager,
    private val repository: AttendanceRepository
) : CoroutineWorker(context, params) {

    companion object {
        // 工作时间范围：早上9点到下午6点半
        private val WORK_START_TIME = LocalTime.of(9, 0)
        private val WORK_END_TIME = LocalTime.of(18, 30)
    }

    override suspend fun doWork(): Result {
        return try {
            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val today = now.toLocalDate()

            // 检查当前时间是否在工作时间范围内（9:00 - 18:30）
            if (currentTime.isBefore(WORK_START_TIME) || currentTime.isAfter(WORK_END_TIME)) {
                // 不在工作时间范围内，不执行自动记录
                // 如果是在工作时间之前，安排到工作时间再检查
                // 如果是在工作时间之后，安排到明天
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

            // 检查是否是工作日（周一到周五）
            if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                // 周末不执行，直接安排下周一的检查
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

            // 检查今天是否已有WFO记录
            val todayRecord = repository.getTodayRecord()

            // 如果今天已经有WFO记录，不再自动定位和记录
            if (todayRecord != null && todayRecord.workMode == WorkMode.WFO) {
                // 重新安排明天的检查
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }

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
                        note = applicationContext.getString(
                            R.string.location_note_wfo,
                            NativeLocationManager.OFFICE_NAME,
                            distance.toInt()
                        )
                    )

                    NotificationHelper.showAttendanceNotification(
                        applicationContext,
                        LocalDate.now(),
                        applicationContext.getString(R.string.notification_title_location_updated),
                        applicationContext.getString(R.string.notification_message_wfo_updated)
                    )

                    // 检测成功，安排明天的检查
                    DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                    return Result.success()
                } else {
                    // 不在办公室，记录WFH（只有没有记录时才记录）
                    if (todayRecord == null) {
                        repository.recordAttendance(
                            date = LocalDate.now(),
                            isPresent = true,
                            workMode = WorkMode.WFH,
                            type = RecordType.AUTO,
                            note = applicationContext.getString(
                                R.string.location_note_wfh,
                                NativeLocationManager.OFFICE_NAME,
                                distance.toInt()
                            )
                        )

                        NotificationHelper.showAttendanceNotification(
                            applicationContext,
                            LocalDate.now(),
                            applicationContext.getString(R.string.notification_title_location_updated),
                            applicationContext.getString(R.string.notification_message_wfh_updated)
                        )
                    }

                    // 检测成功，安排明天的检查
                    DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                    return Result.success()
                }
            } else {
                // 位置获取失败，15分钟后重试
                DailyCheckScheduler.scheduleRetry(applicationContext)
                return Result.retry()
            }
        } catch (e: Exception) {
            // 出错时15分钟后重试
            DailyCheckScheduler.scheduleRetry(applicationContext)
            Result.retry()
        }
    }
}
