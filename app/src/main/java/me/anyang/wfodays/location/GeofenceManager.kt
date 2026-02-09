package me.anyang.wfodays.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.utils.Constants
import me.anyang.wfodays.utils.LanguageManager
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
        val OFFICE_LATITUDE: Double = Constants.OFFICE_LATITUDE
        val OFFICE_LONGITUDE: Double = Constants.OFFICE_LONGITUDE
        val GEOFENCE_RADIUS_METERS: Float = Constants.OFFICE_RADIUS_METERS
        val OFFICE_NAME: String = Constants.OFFICE_NAME
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
                    // 获取配置好语言的 Context，确保通知显示正确的语言
                    val localizedContext = LanguageManager.getLocalizedContext(context)
                    
                    repository.recordAttendance(
                        date = today,
                        isPresent = true,
                        workMode = WorkMode.WFO,
                        type = RecordType.AUTO,
                        note = localizedContext.getString(R.string.gps_location_format, OFFICE_NAME, distance.toInt())
                    )
                    
                    NotificationHelper.showAttendanceNotification(
                        localizedContext,
                        today,
                        localizedContext.getString(R.string.notification_title_auto_record_wfo),
                        localizedContext.getString(R.string.notification_message_auto_record_wfo, OFFICE_NAME)
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
