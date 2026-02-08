package me.anyang.wfodays.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_settings")

class LanguageManager(private val context: Context) {
    
    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        const val LANGUAGE_CHINESE = "zh"
        const val LANGUAGE_ENGLISH = "en"
    }
    
    val currentLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: LANGUAGE_CHINESE
        }
    
    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
    
    fun getLocale(languageCode: String): Locale {
        return when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.SIMPLIFIED_CHINESE
        }
    }
}
