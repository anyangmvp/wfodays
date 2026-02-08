package me.anyang.wfodays.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import me.anyang.wfodays.data.local.LanguageManager
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languageManager: LanguageManager
) {
    
    fun setLocale(languageCode: String): Context {
        val locale = languageManager.getLocale(languageCode)
        return updateResources(context, locale)
    }
    
    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
    }
    
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}
