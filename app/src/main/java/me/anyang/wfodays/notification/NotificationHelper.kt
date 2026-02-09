package me.anyang.wfodays.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import me.anyang.wfodays.MainActivity
import me.anyang.wfodays.R
import java.time.LocalDate

object NotificationHelper {
    const val CHANNEL_ID = "attendance_channel"
    private const val NOTIFICATION_ID_ATTENDANCE_BASE = 1001
    
    fun showAttendanceNotification(
        context: Context,
        date: LocalDate,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 根据通知标题选择相应的颜色
        val notificationColor = getNotificationColor(title)
        
        // 创建更丰富的通知样式
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(title))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(BigTextStyle().bigText(message)) // 大文本样式
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 提高优先级
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColorized(true) // 启用彩色背景
            .setColor(notificationColor) // 设置通知颜色
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 使用时间戳作为唯一ID，避免通知被覆盖
        val uniqueId = (System.currentTimeMillis() / 1000).toInt()
        notificationManager.notify(uniqueId, notification)
    }
    
    // 根据通知标题选择相应的颜色，与应用主题保持一致
    private fun getNotificationColor(title: String): Int {
        return when {
            title.contains("✅") || title.contains("打卡成功") || title.contains("office") || title.contains("wfo") || title.contains("WFO") -> {
                // PrimaryBlue: 0xFF2563EB
                Color.parseColor("#2563EB")
            }
            title.contains("🏠") || title.contains("远程办公") || title.contains("home") || title.contains("wfh") || title.contains("WFH") -> {
                // SuccessGreen: 0xFF10B981
                Color.parseColor("#10B981")
            }
            title.contains("⚠️") || title.contains("系统提醒") || title.contains("system") || title.contains("issue") || title.contains("error") -> {
                // ErrorRed: 0xFFEF4444
                Color.parseColor("#EF4444")
            }
            else -> {
                // 默认颜色: NeutralGray500: 0xFF64748B
                Color.parseColor("#64748B")
            }
        }
    }
    
    // 根据通知标题选择不同的图标
    private fun getNotificationIcon(title: String): Int {
        return when {
            title.contains("✅") && (title.contains("打卡成功") || title.contains("success")) -> android.R.drawable.presence_online
            title.contains("🏠") && (title.contains("远程办公") || title.contains("recorded")) -> android.R.drawable.ic_menu_myplaces
            title.contains("🕒") && (title.contains("非工作时间") || title.contains("overtime")) -> android.R.drawable.ic_menu_recent_history
            title.contains("📅") && (title.contains("周末休息") || title.contains("weekend")) -> android.R.drawable.ic_menu_month
            title.contains("✅") && (title.contains("今日已完成") || title.contains("completed")) -> android.R.drawable.checkbox_on_background
            title.contains("🔄") && (title.contains("正在重试") || title.contains("retrying")) -> android.R.drawable.ic_menu_rotate
            title.contains("⚠️") && (title.contains("系统提醒") || title.contains("alert") || title.contains("issue")) -> android.R.drawable.ic_dialog_alert
            else -> android.R.drawable.ic_dialog_info
        }
    }
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH // 提高重要性等级
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                setShowBadge(true)
                
                // 设置通知灯效
                enableLights(true)
                lightColor = Color.parseColor("#2563EB") // 使用主色调蓝色
                
                // 设置震动模式
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
