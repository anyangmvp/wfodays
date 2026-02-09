package me.anyang.wfodays.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        val notificationStyle = getNotificationStyle(title)
        
        // 创建更丰富的通知样式
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
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
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏显示
            .setCategory(NotificationCompat.CATEGORY_EVENT) // 分类为事件
        
        // 添加操作按钮
        addNotificationActions(builder, context, title)
        
        val notification = builder.build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 使用时间戳作为唯一ID，避免通知被覆盖
        val uniqueId = (System.currentTimeMillis() / 1000).toInt()
        notificationManager.notify(uniqueId, notification)
    }
    
    // 通知样式数据类
    data class NotificationStyle(
        val primaryColor: Int,
        val lightColor: Int,
        val darkColor: Int,
        val iconRes: Int,
        val accentIcon: Int
    )
    
    // 根据通知标题选择相应的颜色，使用更柔和的配色方案
    private fun getNotificationColor(title: String): Int {
        return when {
            title.contains("✅") || title.contains("打卡成功") || title.contains("office") || title.contains("wfo") || title.contains("WFO") -> {
                // 柔和的蓝色 - 办公打卡
                Color.parseColor("#3B82F6")
            }
            title.contains("🏠") || title.contains("远程办公") || title.contains("home") || title.contains("wfh") || title.contains("WFH") -> {
                // 柔和的绿色 - 居家办公
                Color.parseColor("#10B981")
            }
            title.contains("⚠️") || title.contains("系统提醒") || title.contains("system") || title.contains("issue") || title.contains("error") -> {
                // 柔和的橙色 - 提醒警告
                Color.parseColor("#F59E0B")
            }
            title.contains("📍") || title.contains("位置") || title.contains("location") -> {
                // 柔和的紫色 - 位置相关
                Color.parseColor("#8B5CF6")
            }
            else -> {
                // 柔和的蓝灰色 - 默认
                Color.parseColor("#6366F1")
            }
        }
    }
    
    // 获取完整的通知样式配置
    private fun getNotificationStyle(title: String): NotificationStyle {
        return when {
            title.contains("✅") || title.contains("打卡成功") || title.contains("office") || title.contains("wfo") || title.contains("WFO") -> {
                NotificationStyle(
                    primaryColor = Color.parseColor("#3B82F6"),
                    lightColor = Color.parseColor("#DBEAFE"),
                    darkColor = Color.parseColor("#1E40AF"),
                    iconRes = android.R.drawable.ic_menu_mylocation,
                    accentIcon = android.R.drawable.ic_menu_mylocation
                )
            }
            title.contains("🏠") || title.contains("远程办公") || title.contains("home") || title.contains("wfh") || title.contains("WFH") -> {
                NotificationStyle(
                    primaryColor = Color.parseColor("#10B981"),
                    lightColor = Color.parseColor("#D1FAE5"),
                    darkColor = Color.parseColor("#065F46"),
                    iconRes = android.R.drawable.ic_menu_myplaces,
                    accentIcon = android.R.drawable.ic_menu_myplaces
                )
            }
            title.contains("⚠️") || title.contains("系统提醒") || title.contains("alert") -> {
                NotificationStyle(
                    primaryColor = Color.parseColor("#F59E0B"),
                    lightColor = Color.parseColor("#FEF3C7"),
                    darkColor = Color.parseColor("#92400E"),
                    iconRes = android.R.drawable.ic_dialog_alert,
                    accentIcon = android.R.drawable.ic_dialog_alert
                )
            }
            else -> {
                NotificationStyle(
                    primaryColor = Color.parseColor("#6366F1"),
                    lightColor = Color.parseColor("#E0E7FF"),
                    darkColor = Color.parseColor("#3730A3"),
                    iconRes = android.R.drawable.ic_dialog_info,
                    accentIcon = android.R.drawable.ic_dialog_info
                )
            }
        }
    }
    
    // 根据通知标题选择不同的图标
    private fun getNotificationIcon(title: String): Int {
        return when {
            title.contains("✅") && (title.contains("打卡成功") || title.contains("success")) -> 
                android.R.drawable.ic_menu_mylocation
            title.contains("🏠") && (title.contains("远程办公") || title.contains("recorded")) -> 
                android.R.drawable.ic_menu_myplaces
            title.contains("🕒") && (title.contains("非工作时间") || title.contains("overtime")) -> 
                android.R.drawable.ic_menu_recent_history
            title.contains("📅") && (title.contains("周末休息") || title.contains("weekend")) -> 
                android.R.drawable.ic_menu_month
            title.contains("✅") && (title.contains("今日已完成") || title.contains("completed")) -> 
                android.R.drawable.checkbox_on_background
            title.contains("🔄") && (title.contains("正在重试") || title.contains("retrying")) -> 
                android.R.drawable.ic_menu_rotate
            title.contains("⚠️") && (title.contains("系统提醒") || title.contains("alert") || title.contains("issue")) -> 
                android.R.drawable.ic_dialog_alert
            title.contains("📍") || title.contains("位置") || title.contains("location") -> 
                android.R.drawable.ic_menu_mylocation
            else -> android.R.drawable.ic_dialog_info
        }
    }
    
    // 添加通知操作按钮
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        context: Context,
        title: String
    ) {
        // 添加"查看详情"操作
        val viewIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "view_details")
        }
        val viewPendingIntent = PendingIntent.getActivity(
            context,
            1,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        builder.addAction(
            android.R.drawable.ic_menu_view,
            context.getString(R.string.notification_action_view),
            viewPendingIntent
        )
        
        // 根据通知类型添加特定操作
        when {
            title.contains("打卡") || title.contains("record") -> {
                // 添加"查看记录"操作
                val recordIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("action", "view_record")
                }
                val recordPendingIntent = PendingIntent.getActivity(
                    context,
                    2,
                    recordIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_menu_month,
                    context.getString(R.string.notification_action_record),
                    recordPendingIntent
                )
            }
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
                
                // 设置通知灯效 - 使用渐变色效果
                enableLights(true)
                lightColor = Color.parseColor("#3B82F6") // 柔和的蓝色
                
                // 设置震动模式 - 更优雅的节奏
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                
                // 设置锁屏显示
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                
                // 设置声音
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
