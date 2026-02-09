package me.anyang.wfodays.utils

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

    // 上午问候语 (6:00 - 11:59)
    private val morningGreetings = listOf(
        "早安！开启充满活力的一天",
        "早上好！愿今天工作顺利",
        "新的一天，新的起点，加油！",
        "晨光正好，正是奋斗时",
        "早安！愿你今天收获满满",
        "一日之计在于晨，开始吧！",
        "早上好！保持微笑面对挑战",
        "新的一天，愿你能量满满",
        "早安！今天也要全力以赴",
        "美好的一天从早安开始"
    )

    // 中午问候语 (12:00 - 13:59)
    private val noonGreetings = listOf(
        "午安！记得按时吃饭哦",
        "中午好！适当休息，下午更有精神",
        "午餐时间到，补充能量继续战斗",
        "午间小憩，为下午充电",
        "中午好！劳逸结合效率高",
        "午休片刻，精神百倍",
        "午安！愿你下午工作顺利",
        "午餐愉快！保持好心情",
        "中午好！休息是为了更好的出发",
        "午间时光，享受片刻宁静"
    )

    // 下午问候语 (14:00 - 17:59)
    private val afternoonGreetings = listOf(
        "下午好！保持专注，胜利在望",
        "午后时光，继续加油！",
        "下午好！愿你工作效率满满",
        "一天的下半场，全力以赴",
        "下午好！坚持就是胜利",
        "午后工作状态如何？继续冲！",
        "下午好！离下班又近了一步",
        "保持节奏，下午也要精彩",
        "下午好！愿你事事顺心",
        "午后时光，不负韶华"
    )

    // 晚上问候语 (18:00 - 23:59)
    private val eveningGreetings = listOf(
        "晚上好！辛苦了一天，好好休息",
        "夜幕降临，愿你放松身心",
        "晚上好！今天的工作很出色",
        "夜晚时光，享受属于自己的时间",
        "晚上好！愿你有个美好的夜晚",
        "一天结束，为自己点个赞",
        "晚上好！明天又是新的一天",
        "夜色温柔，愿你心情愉快",
        "晚上好！感谢今天的努力",
        "夜幕降临，愿你安然入梦"
    )

    // 凌晨问候语 (00:00 - 5:59)
    private val nightGreetings = listOf(
        "夜深了，注意休息",
        "凌晨时光，愿你安然入睡",
        "夜猫子你好，记得早点休息",
        "深夜时分，保重身体",
        "凌晨了，新的一天即将开始",
        "夜色深沉，愿你有个好梦",
        "深夜工作也要注意身体",
        "凌晨问候，愿你不负时光",
        "夜深人静，愿你心境平和",
        "凌晨时分，注意休息"
    )

    // 周末问候语
    private val weekendGreetings = listOf(
        "周末愉快！享受美好时光",
        "周末到了，好好放松一下吧",
        "周末快乐！做自己喜欢的事",
        "周末时光，愿你心情愉悦",
        "周末愉快！与家人朋友共度美好",
        "周末到了，给自己放个假",
        "周末快乐！享受生活的美好",
        "周末时光，愿你充电满满",
        "周末愉快！愿你笑口常开",
        "周末到了，愿你不负好时光"
    )

    // English Morning Greetings (6:00 - 11:59)
    private val morningGreetingsEn = listOf(
        "Good morning! Start your day with energy.",
        "Rise and shine! Make today productive.",
        "Morning! Fresh start, fresh goals.",
        "Good morning! Ready to tackle the day?",
        "A new day begins. Let's make it count.",
        "Good morning! Embrace the opportunities ahead.",
        "Rise and grind! Success awaits.",
        "Morning! Today is full of possibilities.",
        "Good morning! Stay positive and focused.",
        "A fresh morning brings fresh chances."
    )

    // English Noon Greetings (12:00 - 13:59)
    private val noonGreetingsEn = listOf(
        "Good afternoon! Take a break.",
        "Lunch time! Recharge your energy.",
        "Midday check-in. Keep going strong.",
        "Pause and refresh. You've got this.",
        "Afternoon! Stay focused and balanced.",
        "Good afternoon! Halfway through the day.",
        "Take a breath. You're doing great.",
        "Lunch break! Fuel up for later.",
        "Midday moment. Reflect and continue.",
        "Good afternoon! Keep the momentum."
    )

    // English Afternoon Greetings (14:00 - 17:59)
    private val afternoonGreetingsEn = listOf(
        "Good afternoon! Maintain your momentum.",
        "Stay productive. The day's in full swing.",
        "Afternoon! Keep pushing forward.",
        "You're doing great. Finish strong.",
        "Productive afternoon ahead!",
        "Good afternoon! Stay on track.",
        "The day's not over. Keep shining.",
        "Afternoon energy! Let's get things done.",
        "Stay focused. Success is near.",
        "Good afternoon! Make every minute count."
    )

    // English Evening Greetings (18:00 - 23:59)
    private val eveningGreetingsEn = listOf(
        "Good evening! Time to unwind.",
        "Great work today. Enjoy your evening.",
        "Evening! Relax and recharge.",
        "Well done today. Rest well.",
        "Good evening! You earned this break.",
        "The day is done. Be proud of yourself.",
        "Evening! Time for yourself now.",
        "Good evening! Reflect on today's wins.",
        "Relax and enjoy. Tomorrow's a new day.",
        "Evening! Peaceful moments ahead."
    )

    // English Night Greetings (00:00 - 5:59)
    private val nightGreetingsEn = listOf(
        "Late night! Get some rest.",
        "Time to sleep. Tomorrow awaits.",
        "Rest up. Big day ahead.",
        "Night owl mode. Don't overdo it.",
        "Quiet hours. Recharge now.",
        "Good night! Sweet dreams.",
        "Rest well. You deserve it.",
        "Night time. Let go of today's stress.",
        "Sleep tight. See you tomorrow.",
        "Late hours. Prioritize your rest."
    )

    // English Weekend Greetings
    private val weekendGreetingsEn = listOf(
        "Happy weekend! Enjoy your time off.",
        "Weekend vibes! Do what you love.",
        "It's the weekend! Relax and recharge.",
        "Weekend mode on. Make it memorable.",
        "Enjoy your weekend! You've earned it.",
        "Happy weekend! Adventure awaits.",
        "Weekend! Time for fun and rest.",
        "It's the weekend. Live in the moment.",
        "Happy weekend! Create great memories.",
        "Weekend joy! Embrace the freedom."
    )

    /**
     * 获取当前时间段的随机问候语（根据语言自动选择）
     */
    fun getGreeting(languageCode: String = "zh"): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        val dayOfWeek = now.dayOfWeek

        val isEnglish = languageCode == "en"

        // 周末优先显示周末问候语
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return if (Random.nextBoolean()) {
                if (isEnglish) weekendGreetingsEn.random() else weekendGreetings.random()
            } else {
                if (isEnglish) getTimeBasedGreetingEn(hour) else getTimeBasedGreeting(hour)
            }
        }

        return if (isEnglish) getTimeBasedGreetingEn(hour) else getTimeBasedGreeting(hour)
    }

    private fun getTimeBasedGreeting(hour: Int): String {
        return when (hour) {
            in 6..11 -> morningGreetings.random()
            in 12..13 -> noonGreetings.random()
            in 14..17 -> afternoonGreetings.random()
            in 18..23 -> eveningGreetings.random()
            else -> nightGreetings.random() // 0-5点
        }
    }

    private fun getTimeBasedGreetingEn(hour: Int): String {
        return when (hour) {
            in 6..11 -> morningGreetingsEn.random()
            in 12..13 -> noonGreetingsEn.random()
            in 14..17 -> afternoonGreetingsEn.random()
            in 18..23 -> eveningGreetingsEn.random()
            else -> nightGreetingsEn.random() // 0-5点
        }
    }

    /**
     * 获取当前时间段名称
     */
    fun getTimePeriod(): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        val dayOfWeek = now.dayOfWeek

        // 周末
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
