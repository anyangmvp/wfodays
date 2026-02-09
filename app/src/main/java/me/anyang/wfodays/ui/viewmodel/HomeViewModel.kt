package me.anyang.wfodays.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.data.repository.MonthlyStatistics
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    companion object {
        // 工作时间范围：早上9点到下午6点半
        private val WORK_START_TIME = LocalTime.of(9, 0)
        private val WORK_END_TIME = LocalTime.of(18, 30)
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 检查今天是否已有记录，如果没有则尝试自动检测位置
                val todayRecord = repository.getTodayRecord()
                if (todayRecord == null) {
                    // 今天还没有记录，尝试根据位置自动记录
                    autoDetectLocationAndRecord()
                }

                val updatedTodayRecord = repository.getTodayRecord()
                val currentMonthStats = repository.getMonthlyStatistics(YearMonth.now())

                repository.getRecentRecords(10).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        todayRecord = updatedTodayRecord,
                        currentMonthStats = currentMonthStats,
                        recentRecords = records,
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

    /**
     * 根据当前位置自动检测并记录今日状态
     * 只在工作时间（9:00-18:30）且今天还没有WFO记录时触发
     */
    private suspend fun autoDetectLocationAndRecord() {
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()
        val today = now.toLocalDate()

        // 检查当前时间是否在工作时间范围内（9:00 - 18:30）
        if (currentTime.isBefore(WORK_START_TIME) || currentTime.isAfter(WORK_END_TIME)) {
            // 不在工作时间范围内，不执行自动记录
            return
        }

        // 检查是否是工作日（周一到周五）
        if (today.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
            // 周末不执行自动记录
            return
        }

        // 检查今天是否已有WFO记录
        val todayRecord = repository.getTodayRecord()
        if (todayRecord != null && todayRecord.workMode == WorkMode.WFO) {
            // 今天已经有WFO记录，不再自动记录
            return
        }

        // 检查是否有位置权限
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        var latitude = 0.0
        var longitude = 0.0

        withContext(Dispatchers.Main) {
            locationManager.getCurrentLocation { lat, lon, _ ->
                latitude = lat
                longitude = lon
            }
        }

        // 等待位置回调
        delay(2000)

        if (latitude != 0.0 && longitude != 0.0) {
            val distance = locationManager.calculateDistanceToOffice(latitude, longitude)
            val isInOffice = distance <= NativeLocationManager.OFFICE_RADIUS_METERS

            if (isInOffice) {
                // 在公司，记录WFO
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
            } else {
                // 在家，记录WFH（只有没有记录时才记录）
                if (todayRecord == null) {
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
                }
            }
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

    /**
     * 切换今日状态（WFO -> WFH -> LEAVE -> WFO）
     */
    fun toggleTodayStatus() {
        viewModelScope.launch {
            try {
                val todayRecord = repository.getTodayRecord()
                val currentMode = todayRecord?.workMode

                // 循环切换：WFO -> WFH -> LEAVE -> WFO
                val nextMode = when (currentMode) {
                    WorkMode.WFO -> WorkMode.WFH
                    WorkMode.WFH -> WorkMode.LEAVE
                    WorkMode.LEAVE -> WorkMode.WFO
                    null -> WorkMode.WFO
                }

                val note = when (nextMode) {
                    WorkMode.WFO -> context.getString(R.string.manual_switch_wfo)
                    WorkMode.WFH -> context.getString(R.string.manual_switch_wfh)
                    WorkMode.LEAVE -> context.getString(R.string.manual_switch_leave)
                }

                repository.markWorkMode(
                    date = LocalDate.now(),
                    workMode = nextMode,
                    note = note
                )
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class HomeUiState(
    val todayRecord: AttendanceRecord? = null,
    val currentMonthStats: MonthlyStatistics? = null,
    val recentRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
