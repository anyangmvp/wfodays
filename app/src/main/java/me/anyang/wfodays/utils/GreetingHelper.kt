package me.anyang.wfodays.utils

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.random.Random
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.anyang.wfodays.R

/**
 * 问候语帮助类
 * 根据时间段显示不同的随机问候语
 */
object GreetingHelper {

    /**
     * 获取当前时间段的随机问候语
     */
    fun getGreeting(context: Context): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        val dayOfWeek = now.dayOfWeek

        // 周末优先显示周末问候语
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            if (Random.nextBoolean()) {
                return getRandomGreeting(context, R.array.greetings_weekend)
            }
        }

        return when (hour) {
            in 6..11 -> getRandomGreeting(context, R.array.greetings_morning)
            in 12..13 -> getRandomGreeting(context, R.array.greetings_noon)
            in 14..17 -> getRandomGreeting(context, R.array.greetings_afternoon)
            in 18..23 -> getRandomGreeting(context, R.array.greetings_evening)
            else -> getRandomGreeting(context, R.array.greetings_night)
        }
    }

    private fun getRandomGreeting(context: Context, arrayResId: Int): String {
        val greetings = context.resources.getStringArray(arrayResId)
        return greetings.random()
    }

    /**
     * 获取当前时间段名称
     */
    fun getTimePeriod(): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        val dayOfWeek = now.dayOfWeek

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return "weekend"
        }

        return when (hour) {
            in 6..11 -> "morning"
            in 12..13 -> "noon"
            in 14..17 -> "afternoon"
            in 18..23 -> "evening"
            else -> "midnight"
        }
    }

    /**
     * 获取本地化的时间段名称
     */
    @Composable
    fun getLocalizedTimePeriod(): String {
        val timePeriod = getTimePeriod()
        return when (timePeriod) {
            "morning" -> stringResource(R.string.morning)
            "noon" -> stringResource(R.string.noon)
            "afternoon" -> stringResource(R.string.afternoon)
            "evening" -> stringResource(R.string.evening)
            "midnight" -> stringResource(R.string.midnight)
            "weekend" -> stringResource(R.string.weekend_greeting)
            else -> stringResource(R.string.morning)
        }
    }
}
