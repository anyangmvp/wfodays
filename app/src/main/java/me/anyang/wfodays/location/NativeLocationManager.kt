package me.anyang.wfodays.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class NativeLocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 中软国际办公楼坐标
    companion object {
        // 34°12'35.3"N = 34 + 12/60 + 35.3/3600 = 34.2098056
        const val OFFICE_LATITUDE = 34.2098056
        // 108°50'16.6"E = 108 + 50/60 + 16.6/3600 = 108.8379444
        const val OFFICE_LONGITUDE = 108.8379444
        const val OFFICE_RADIUS_METERS = 800f
        const val OFFICE_NAME = "中软国际"
    }

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState

    private var locationListener: LocationListener? = null

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _locationState.value = LocationState.Error("定位权限未授权")
            return
        }

        _locationState.value = LocationState.Loading

        // 尝试获取GPS和网络定位
        try {
            // GPS定位（高精度）
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L, // 5秒更新一次
                10f,   // 移动10米才更新
                createLocationListener(),
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // 网络定位（低精度但室内可用）
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000L,
                10f,
                createLocationListener(),
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 立即获取最后已知位置
        getLastKnownLocation()
    }

    fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
        locationListener = null
    }

    private fun createLocationListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                _locationState.value = LocationState.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    provider = location.provider ?: "unknown"
                )
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }.also {
            locationListener = it
        }
    }

    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 尝试获取GPS最后位置
        val gpsLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) { null }

        // 尝试获取网络最后位置
        val networkLocation = try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) { null }

        // 选择最新的位置
        val bestLocation = when {
            gpsLocation != null && networkLocation != null ->
                if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
            gpsLocation != null -> gpsLocation
            networkLocation != null -> networkLocation
            else -> null
        }

        bestLocation?.let {
            _locationState.value = LocationState.Success(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracy = it.accuracy,
                provider = it.provider ?: "last_known"
            )
        }
    }

    // 使用Haversine公式计算两点间距离（米）
    fun calculateDistanceToOffice(latitude: Double, longitude: Double): Float {
        return calculateDistance(
            latitude, longitude,
            OFFICE_LATITUDE, OFFICE_LONGITUDE
        )
    }

    fun isWithinOfficeRadius(latitude: Double, longitude: Double): Boolean {
        return calculateDistanceToOffice(latitude, longitude) <= OFFICE_RADIUS_METERS
    }

    // Haversine公式计算地球表面两点距离
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val R = 6371000.0 // 地球半径（米）
        
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (R * c).toFloat()
    }

    // 获取当前位置并回调
    fun getCurrentLocation(callback: (Double, Double, Float) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(0.0, 0.0, 0f)
            return
        }

        val gpsLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) { null }

        val networkLocation = try {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) { null }

        val bestLocation = when {
            gpsLocation != null && networkLocation != null ->
                if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
            gpsLocation != null -> gpsLocation
            networkLocation != null -> networkLocation
            else -> null
        }

        bestLocation?.let {
            callback(it.latitude, it.longitude, it.accuracy)
        } ?: callback(0.0, 0.0, 0f)
    }

    sealed class LocationState {
        object Idle : LocationState()
        object Loading : LocationState()
        data class Success(
            val latitude: Double,
            val longitude: Double,
            val accuracy: Float,
            val provider: String = "unknown"
        ) : LocationState()
        data class Error(val message: String) : LocationState()
    }
}
