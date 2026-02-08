package me.anyang.wfodays.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.data.repository.AttendanceRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun loadMonthData(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentMonth = yearMonth,
                isLoading = true
            )
            
            try {
                repository.getMonthlyRecords(yearMonth).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        monthRecords = records,
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

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        loadMonthData(newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        loadMonthData(newMonth)
    }

    fun markWorkMode(date: LocalDate, workMode: WorkMode) {
        viewModelScope.launch {
            try {
                repository.markWorkMode(date, workMode)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteRecord(date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(date)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val monthRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
