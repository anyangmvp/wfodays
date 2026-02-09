package me.anyang.wfodays.location

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.utils.LanguageManager
import java.time.LocalDate

/**
 * 基于位置的考勤记录器
 * 统一处理获取位置、计算距离、记录考勤的逻辑
 */
class LocationBasedAttendanceRecorder(
    private val context: Context,
    private val repository: AttendanceRepository,
    private val locationManager: NativeLocationManager
) {
    companion object {
        private const val TAG = "LocationRecorder"
        private const val LOCATION_TIMEOUT_MS = 3000L
    }

    /**
     * 位置记录结果
     */
    sealed class RecordResult {
        data class Success(val workMode: WorkMode, val distance: Float) : RecordResult()
        data class Skipped(val reason: String) : RecordResult()
        data class Error(val exception: Exception) : RecordResult()
    }

    /**
     * 检测位置并根据位置记录考勤
     *
     * @param skipExistingCheck 是否跳过已有记录检查
     * @param showNotification 是否显示通知
     * @return 记录结果
     */
    suspend fun detectAndRecord(
        skipExistingCheck: Boolean = false,
        showNotification: Boolean = false
    ): RecordResult {
        return try {
            // 获取当前位置
            val locationResult = getCurrentLocation()

            if (locationResult == null) {
                Log.d(TAG, "位置获取失败")
                return RecordResult.Skipped("位置获取失败")
            }

            val (latitude, longitude) = locationResult
            val distance = locationManager.calculateDistanceToOffice(latitude, longitude)
            val isInOffice = distance <= NativeLocationManager.OFFICE_RADIUS_METERS

            // 检查今天的记录
            val todayRecord = repository.getTodayRecord()

            if (isInOffice) {
                // 在公司，记录WFO
                recordWFO(distance)
                if (showNotification) {
                    showWfoNotification(distance)
                }
                RecordResult.Success(WorkMode.WFO, distance)
            } else {
                // 在家，记录WFH（根据条件判断是否记录）
                if (skipExistingCheck || todayRecord == null) {
                    recordWFH(distance)
                    if (showNotification) {
                        showWfhNotification(distance)
                    }
                    RecordResult.Success(WorkMode.WFH, distance)
                } else {
                    RecordResult.Skipped("已有记录且非强制更新")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "记录考勤时出错: ${e.message}", e)
            RecordResult.Error(e)
        }
    }

    /**
     * 获取当前位置
     * @return 位置坐标 (latitude, longitude)，获取失败返回 null
     */
    private suspend fun getCurrentLocation(): Pair<Double, Double>? {
        var latitude = 0.0
        var longitude = 0.0

        withContext(Dispatchers.Main) {
            locationManager.getCurrentLocation { lat, lon, _ ->
                latitude = lat
                longitude = lon
            }
        }

        // 等待位置回调
        delay(LOCATION_TIMEOUT_MS)

        return if (latitude != 0.0 || longitude != 0.0) {
            Pair(latitude, longitude)
        } else {
            null
        }
    }

    /**
     * 记录WFO
     */
    private suspend fun recordWFO(distance: Float) {
        repository.recordAttendance(
            date = LocalDate.now(),
            isPresent = true,
            workMode = WorkMode.WFO,
            type = RecordType.AUTO,
            note = context.getString(
                R.string.location_note_wfo,
                NativeLocationManager.OFFICE_NAME,
                distance.toInt()
            )
        )
        Log.d(TAG, "WFO记录成功，距离: ${distance.toInt()}米")
    }

    /**
     * 记录WFH
     */
    private suspend fun recordWFH(distance: Float) {
        repository.recordAttendance(
            date = LocalDate.now(),
            isPresent = true,
            workMode = WorkMode.WFH,
            type = RecordType.AUTO,
            note = context.getString(
                R.string.location_note_wfh,
                NativeLocationManager.OFFICE_NAME,
                distance.toInt()
            )
        )
        Log.d(TAG, "WFH记录成功，距离: ${distance.toInt()}米")
    }

    /**
     * 显示WFO通知
     */
    private fun showWfoNotification(distance: Float) {
        val localizedContext = LanguageManager.getLocalizedContext(context)
        val distanceDisplay = formatDistance(distance)

        NotificationHelper.showAttendanceNotification(
            localizedContext,
            LocalDate.now(),
            localizedContext.getString(R.string.notification_title_wfo_success),
            localizedContext.getString(R.string.notification_message_office_distance, distanceDisplay)
        )
    }

    /**
     * 显示WFH通知
     */
    private fun showWfhNotification(distance: Float) {
        val localizedContext = LanguageManager.getLocalizedContext(context)
        val distanceDisplay = formatDistance(distance)

        NotificationHelper.showAttendanceNotification(
            localizedContext,
            LocalDate.now(),
            localizedContext.getString(R.string.notification_title_wfh_recorded),
            localizedContext.getString(R.string.notification_message_home_distance, distanceDisplay)
        )
    }

    /**
     * 格式化距离显示
     */
    private fun formatDistance(distance: Float): String {
        return if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
            val roundedDistance = ((distance / 100).toInt()) * 100
            context.getString(R.string.distance_format_string, roundedDistance)
        } else {
            context.getString(R.string.distance_kilometer_format, distance / 1000)
        }
    }
}
