package me.anyang.wfodays.utils

import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.WorkMode
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
    
    /**
     * 计算剩余工作日，排除已标记为假期的日期和今天
     * @param yearMonth 年月
     * @param fromDate 起始日期（今天，会被排除）
     * @param records 本月所有考勤记录
     * @return 排除假期和今天后的剩余工作日数
     */
    fun getRemainingWorkdaysExcludingLeaves(
        yearMonth: YearMonth, 
        fromDate: LocalDate = LocalDate.now(),
        records: List<AttendanceRecord>
    ): Int {
        var count = 0
        var date = fromDate.plusDays(1)
        val endDate = yearMonth.atEndOfMonth()
        
        val leaveDates = records
            .filter { it.workMode == WorkMode.LEAVE }
            .map { 
                java.time.Instant.ofEpochMilli(it.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
            }
            .toSet()
        
        while (!date.isAfter(endDate)) {
            if (isWorkday(date) && date !in leaveDates) {
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
