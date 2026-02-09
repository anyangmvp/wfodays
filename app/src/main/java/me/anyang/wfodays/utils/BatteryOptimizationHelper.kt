package me.anyang.wfodays.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import me.anyang.wfodays.R

object BatteryOptimizationHelper {

    /**
     * 检查应用是否已加入电池优化白名单
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 请求用户将应用加入电池优化白名单
     * 注意：这需要在 Activity 中调用
     */
    fun requestIgnoreBatteryOptimizations(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * 打开电池优化设置页面
     */
    fun openBatteryOptimizationSettings(context: Context): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * 打开小米自启动管理页面（如果可能）
     */
    fun openAutostartSettings(context: Context): Intent? {
        return try {
            // 尝试打开小米自启动管理
            Intent().apply {
                component = android.content.ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取品牌特定的设置指南
     */
    fun getBrandSpecificGuide(context: Context): String {
        return when {
            isXiaomi() -> {
                context.getString(R.string.xiaomi_guide)
            }
            isOPPO() -> {
                context.getString(R.string.oppo_guide)
            }
            isVivo() -> {
                context.getString(R.string.vivo_guide)
            }
            isHuawei() -> {
                context.getString(R.string.huawei_guide)
            }
            else -> {
                context.getString(R.string.general_guide)
            }
        }
    }

    private fun isXiaomi(): Boolean {
        return android.os.Build.MANUFACTURER.lowercase().contains("xiaomi") ||
               android.os.Build.BRAND.lowercase().contains("xiaomi") ||
               android.os.Build.BRAND.lowercase().contains("redmi")
    }

    private fun isOPPO(): Boolean {
        return android.os.Build.MANUFACTURER.lowercase().contains("oppo") ||
               android.os.Build.BRAND.lowercase().contains("oppo") ||
               android.os.Build.BRAND.lowercase().contains("realme")
    }

    private fun isVivo(): Boolean {
        return android.os.Build.MANUFACTURER.lowercase().contains("vivo") ||
               android.os.Build.BRAND.lowercase().contains("vivo")
    }

    private fun isHuawei(): Boolean {
        return android.os.Build.MANUFACTURER.lowercase().contains("huawei") ||
               android.os.Build.BRAND.lowercase().contains("huawei") ||
               android.os.Build.BRAND.lowercase().contains("honor")
    }
}
