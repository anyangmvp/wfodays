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
import java.time.LocalDate
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
     */
    private suspend fun autoDetectLocationAndRecord() {
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
                // 在家，记录WFH
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

    fun manualCheckIn() {
        viewModelScope.launch {
            try {
                repository.markWorkMode(
                    date = LocalDate.now(),
                    workMode = WorkMode.WFO,
                    note = "手动标记WFO"
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
                    note = "标记为休假"
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
                    note = "标记为WFH"
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
                    WorkMode.WFO -> "手动切换为WFO"
                    WorkMode.WFH -> "手动切换为WFH"
                    WorkMode.LEAVE -> "手动切换为休假"
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
