package me.anyang.wfodays.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_WFO_MODE = booleanPreferencesKey("is_wfo_mode")
        val DEBUG_NOTIFICATION_MODE = booleanPreferencesKey("debug_notification_mode")
        val DEBUG_NOTIFICATION_INTERVAL = intPreferencesKey("debug_notification_interval")
    }
    
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }
    
    val debugNotificationMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DEBUG_NOTIFICATION_MODE] ?: false
        }
    
    val debugNotificationInterval: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DEBUG_NOTIFICATION_INTERVAL] ?: 10  // 默认10分钟
        }
    
    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
    
    suspend fun setDebugNotificationMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEBUG_NOTIFICATION_MODE] = enabled
        }
    }
    
    suspend fun setDebugNotificationInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEBUG_NOTIFICATION_INTERVAL] = minutes
        }
    }
}
