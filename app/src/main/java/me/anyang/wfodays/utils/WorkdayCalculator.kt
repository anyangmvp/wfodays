package me.anyang.wfodays.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

object WorkdayCalculator {
    
    fun calculateWorkdays(yearMonth: YearMonth): Int {
        var count = 0
        var date = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        
        while (!date.isAfter(endDate)) {
            if (isWorkday(date)) {
                count++
            }
            date = date.plusDays(1)
        }
        
        return count
    }
    
    fun isWorkday(date: LocalDate): Boolean {
        return date.dayOfWeek !in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
    
    fun getRemainingWorkdays(yearMonth: YearMonth, fromDate: LocalDate = LocalDate.now()): Int {
        var count = 0
        var date = fromDate
        val endDate = yearMonth.atEndOfMonth()
        
        while (!date.isAfter(endDate)) {
            if (isWorkday(date)) {
                count++
            }
            date = date.plusDays(1)
        }
        
        return count
    }
    
    fun calculateRequiredDays(yearMonth: YearMonth, rate: Double = 0.6): Int {
        val totalWorkdays = calculateWorkdays(yearMonth)
        return kotlin.math.ceil(totalWorkdays * rate).toInt()
    }
}
