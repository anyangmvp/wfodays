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

class DailyLocationCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val locationManager: NativeLocationManager,
    private val repository: AttendanceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 检查是否是工作日（周一到周五）
            val today = LocalDate.now()
            if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                // 周末不执行，直接安排下周一的检查
                DailyCheckScheduler.scheduleDailyCheck(applicationContext)
                return Result.success()
            }
            
            // 检查今天是否已有记录
            val todayRecord = repository.getTodayRecord()
            
            // 如果今天已经有WFO或LEAVE记录，不再自动更新
            if (todayRecord != null && (todayRecord.workMode == WorkMode.WFO || todayRecord.workMode == WorkMode.LEAVE)) {
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
                } else {
                    // 不在办公室，记录WFH
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
            }

            // 重新安排明天的检查
            DailyCheckScheduler.scheduleDailyCheck(applicationContext)
            Result.success()
        } catch (e: Exception) {
            // 出错时重试，并重新安排明天的检查
            DailyCheckScheduler.scheduleDailyCheck(applicationContext)
            Result.retry()
        }
    }
}
