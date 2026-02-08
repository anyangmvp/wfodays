package me.anyang.wfodays.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.anyang.wfodays.data.database.AppDatabase
import me.anyang.wfodays.data.database.AttendanceDao
import me.anyang.wfodays.data.local.LanguageManager
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.data.repository.AttendanceRepository
import me.anyang.wfodays.location.GeofenceManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.utils.LocaleHelper
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(attendanceDao: AttendanceDao): AttendanceRepository {
        return AttendanceRepository(attendanceDao)
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideLanguageManager(@ApplicationContext context: Context): LanguageManager {
        return LanguageManager(context)
    }

    @Provides
    @Singleton
    fun provideNativeLocationManager(@ApplicationContext context: Context): NativeLocationManager {
        return NativeLocationManager(context)
    }

    @Provides
    @Singleton
    fun provideGeofenceManager(
        @ApplicationContext context: Context,
        repository: AttendanceRepository,
        locationManager: NativeLocationManager
    ): GeofenceManager {
        return GeofenceManager(context, repository, locationManager)
    }

    @Provides
    @Singleton
    fun provideLocaleHelper(
        @ApplicationContext context: Context,
        languageManager: LanguageManager
    ): LocaleHelper {
        return LocaleHelper(context, languageManager)
    }
}
