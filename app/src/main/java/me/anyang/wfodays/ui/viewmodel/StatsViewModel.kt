package me.anyang.wfodays.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.data.repository.MonthlyStatistics
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentStats = repository.getMonthlyStatistics(
                    java.time.YearMonth.now()
                )
                val allStats = repository.getAllMonthlyStatistics()
                
                _uiState.value = _uiState.value.copy(
                    currentMonthStats = currentStats,
                    allStats = allStats,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}

data class StatsUiState(
    val currentMonthStats: MonthlyStatistics? = null,
    val allStats: List<MonthlyStatistics> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
