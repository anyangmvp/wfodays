package me.anyang.wfodays.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaiduLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState

    fun initLocationClient() {
        // 模拟实现
    }

    fun startLocation() {
        // 模拟实现
    }

    fun stopLocation() {
        // 模拟实现
    }

    fun requestSingleLocation(callback: (Double, Double) -> Unit) {
        // 模拟返回公司位置
        callback(34.2731, 108.8465)
    }

    sealed class LocationState {
        object Idle : LocationState()
        object Loading : LocationState()
        data class Success(val latitude: Double, val longitude: Double, val address: String) : LocationState()
        data class Error(val message: String) : LocationState()
    }
}
