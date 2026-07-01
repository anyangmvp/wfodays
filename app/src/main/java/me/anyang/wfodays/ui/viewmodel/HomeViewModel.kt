package me.anyang.wfodays.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.data.repository.MonthlyStatistics
import me.anyang.wfodays.location.LocationBasedAttendanceRecorder
import me.anyang.wfodays.location.NativeLocationManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val locationManager: NativeLocationManager,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val locationRecorder by lazy {
        LocationBasedAttendanceRecorder(context, repository, locationManager)
    }

    companion object {
        private val WORK_START_TIME = LocalTime.of(9, 0)
        private val WORK_END_TIME = LocalTime.of(18, 30)
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get target percentage from preferences
                val targetPercentage = preferencesManager.wfoTargetPercentage.first()

                val todayRecord = repository.getTodayRecord()
                if (todayRecord == null) {
                    autoDetectLocationAndRecord()
                }

                val updatedTodayRecord = repository.getTodayRecord()
                val currentMonthStats = repository.getMonthlyStatistics(YearMonth.now())

                repository.getRecentRecords(10).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        todayRecord = updatedTodayRecord,
                        currentMonthStats = currentMonthStats,
                        recentRecords = records,
                        targetPercentage = targetPercentage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun autoDetectLocationAndRecord() {
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()
        val today = now.toLocalDate()

        if (currentTime.isBefore(WORK_START_TIME) || currentTime.isAfter(WORK_END_TIME)) {
            return
        }

        if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            return
        }

        val todayRecord = repository.getTodayRecord()
        if (todayRecord != null && (todayRecord.workMode == WorkMode.WFO || todayRecord.workMode == WorkMode.LEAVE)) {
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val result = locationRecorder.detectAndRecord(skipExistingCheck = false)
        if (result is LocationBasedAttendanceRecorder.RecordResult.Success) {
            loadData()
        }
    }

    fun manualCheckIn() {
        viewModelScope.launch {
            try {
                repository.markWorkMode(
                    date = LocalDate.now(),
                    workMode = WorkMode.WFO,
                    note = context.getString(R.string.manual_mark_wfo)
                )
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markAsLeave() {
        viewModelScope.launch {
            try {
                repository.markWorkMode(
                    date = LocalDate.now(),
                    workMode = WorkMode.LEAVE,
                    note = context.getString(R.string.manual_mark_leave)
                )
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markAsWFH() {
        viewModelScope.launch {
            try {
                repository.markWorkMode(
                    date = LocalDate.now(),
                    workMode = WorkMode.WFH,
                    note = context.getString(R.string.manual_mark_wfh)
                )
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    suspend fun autoDetectAndRecordByLocation(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val result = locationRecorder.detectAndRecord(skipExistingCheck = true)
        val success = result is LocationBasedAttendanceRecorder.RecordResult.Success

        if (success) {
            loadData()
        }

        return success
    }
}

data class HomeUiState(
    val todayRecord: AttendanceRecord? = null,
    val currentMonthStats: MonthlyStatistics? = null,
    val recentRecords: List<AttendanceRecord> = emptyList(),
    val targetPercentage: Float = 30f,
    val isLoading: Boolean = false,
    val error: String? = null
)
