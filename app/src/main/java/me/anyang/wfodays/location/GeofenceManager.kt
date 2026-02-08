package me.anyang.wfodays.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AttendanceRepository,
    private val locationManager: NativeLocationManager
) {
    companion object {
        // 中软国际办公楼坐标
        const val OFFICE_LATITUDE = 34.2098056
        const val OFFICE_LONGITUDE = 108.8379444
        const val GEOFENCE_RADIUS_METERS = 800f
        const val OFFICE_NAME = "中软国际"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isMonitoring = false

    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        locationManager.startLocationUpdates()
        
        // Monitor location updates
        scope.launch {
            locationManager.locationState.collect { state ->
                when (state) {
                    is NativeLocationManager.LocationState.Success -> {
                        checkLocationAndRecord(state.latitude, state.longitude)
                    }
                    else -> { /* Ignore other states */ }
                }
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        locationManager.stopLocationUpdates()
    }

    private fun checkLocationAndRecord(latitude: Double, longitude: Double) {
        val distance = locationManager.calculateDistanceToOffice(latitude, longitude)
        
        if (distance <= GEOFENCE_RADIUS_METERS) {
            // Within office radius, record WFO
            scope.launch {
                val today = LocalDate.now()
                val existingRecord = repository.getTodayRecord()
                
                // Only auto-record if no record exists or not already WFO
                if (existingRecord == null || existingRecord.workMode != WorkMode.WFO) {
                    repository.recordAttendance(
                        date = today,
                        isPresent = true,
                        workMode = WorkMode.WFO,
                        type = RecordType.AUTO,
                        note = "GPS定位：距离${OFFICE_NAME} ${distance.toInt()} 米"
                    )
                    
                    NotificationHelper.showAttendanceNotification(
                        context,
                        today,
                        "自动记录 WFO",
                        "检测到您已到达${OFFICE_NAME}附近，已自动记录今日WFO"
                    )
                }
            }
        }
    }

    fun getCurrentDistance(): Float? {
        var distance: Float? = null
        locationManager.getCurrentLocation { lat, lon, _ ->
            if (lat != 0.0 && lon != 0.0) {
                distance = locationManager.calculateDistanceToOffice(lat, lon)
            }
        }
        return distance
    }
}
