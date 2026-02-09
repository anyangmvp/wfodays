package me.anyang.wfodays.utils

object Constants {
    const val DATABASE_NAME = "wfodays_database"
    const val DATABASE_VERSION = 1
    
    // 公司坐标（环普产业园）
    const val OFFICE_LATITUDE = 34.211892
    const val OFFICE_LONGITUDE = 108.834240
    const val OFFICE_RADIUS_METERS = 800f
    const val OFFICE_NAME = "环普产业园"
    
    // 出勤率要求
    const val REQUIRED_ATTENDANCE_RATE = 0.6
    
    // 通知相关
    const val NOTIFICATION_CHANNEL_ID = "attendance_channel"
    const val DAILY_REMINDER_HOUR = 10
    const val DAILY_REMINDER_MINUTE = 0
    
    // WorkManager
    const val WORK_TAG_LOCATION = "location_monitor"
    const val WORK_INTERVAL_MINUTES = 15L
    
    // 数据存储键
    const val PREF_KEY_FIRST_LAUNCH = "first_launch"
    const val PREF_KEY_BAIDU_KEY_CONFIGURED = "baidu_key_configured"
}
