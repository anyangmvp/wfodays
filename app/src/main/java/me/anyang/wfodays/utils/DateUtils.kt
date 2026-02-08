package me.anyang.wfodays.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    
    fun toEpochMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    fun fromEpochMillis(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
    
    fun formatDate(date: LocalDate, pattern: String = "yyyy年MM月dd日"): String {
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }
    
    fun formatDateShort(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("MM/dd"))
    }
    
    fun getStartOfDayMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    fun getEndOfDayMillis(date: LocalDate): Long {
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    }
}
