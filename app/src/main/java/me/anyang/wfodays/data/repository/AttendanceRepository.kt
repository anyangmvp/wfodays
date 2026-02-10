package me.anyang.wfodays.data.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import me.anyang.wfodays.R
import me.anyang.wfodays.data.database.AttendanceDao
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.utils.DateUtils
import me.anyang.wfodays.utils.WorkdayCalculator
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val context: Context
) {

    suspend fun recordAttendance(
        date: LocalDate,
        isPresent: Boolean = true,
        workMode: WorkMode = WorkMode.WFO,
        type: RecordType = RecordType.AUTO,
        note: String? = null
    ) {
        val record = AttendanceRecord(
            date = DateUtils.toEpochMillis(date),
            isPresent = isPresent,
            workMode = workMode,
            recordType = type,
            location = if (type == RecordType.AUTO) context.getString(R.string.gps_auto_location) else null,
            note = note
        )
        attendanceDao.insertRecord(record)
    }

    suspend fun markWorkMode(date: LocalDate, workMode: WorkMode, note: String? = null) {
        val existing = attendanceDao.getRecordByDate(DateUtils.toEpochMillis(date))
        // isPresent 为 true 表示这一天有记录（WFO 或 LEAVE 或 WFH）
        // 这样 LEAVE 记录也能被统计查询到
        val isPresent = workMode == WorkMode.WFO || workMode == WorkMode.LEAVE || workMode == WorkMode.WFH
        if (existing != null) {
            val updated = existing.copy(
                workMode = workMode,
                isPresent = isPresent,
                recordType = RecordType.MANUAL,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            attendanceDao.insertRecord(updated)
        } else {
            recordAttendance(
                date = date,
                isPresent = isPresent,
                workMode = workMode,
                type = RecordType.MANUAL,
                note = note
            )
        }
    }

    suspend fun deleteRecord(date: LocalDate) {
        attendanceDao.deleteRecordByDate(DateUtils.toEpochMillis(date))
    }

    fun getRecentRecords(limit: Int = 10): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecentRecords(limit)
    }

    fun getMonthlyRecords(yearMonth: YearMonth): Flow<List<AttendanceRecord>> {
        val start = DateUtils.toEpochMillis(yearMonth.atDay(1))
        val end = DateUtils.toEpochMillis(yearMonth.atEndOfMonth())
        return attendanceDao.getRecordsBetween(start, end)
    }

    suspend fun getTodayRecord(): AttendanceRecord? {
        return attendanceDao.getRecordByDate(DateUtils.toEpochMillis(LocalDate.now()))
    }

    suspend fun getMonthlyStatistics(yearMonth: YearMonth): MonthlyStatistics {
        val start = DateUtils.toEpochMillis(yearMonth.atDay(1))
        val end = DateUtils.toEpochMillis(yearMonth.atEndOfMonth())
        
        // Get all records for the month
        val records = attendanceDao.getPresentRecordsBetween(start, end)
        
        // Count WFO days (excluding leave days)
        val wfoDays = records.count { it.workMode == WorkMode.WFO }
        
        // Count leave days
        val leaveDays = records.count { it.workMode == WorkMode.LEAVE }
        
        // Calculate total workdays (excluding weekends)
        val totalWorkdays = WorkdayCalculator.calculateWorkdays(yearMonth)
        
        // Calculate effective workdays: totalWorkdays - leaveDays
        val effectiveWorkdays = totalWorkdays - leaveDays
        
        // Calculate required WFO days: effectiveWorkdays * 0.6
        val requiredDays = kotlin.math.ceil(effectiveWorkdays * 0.6).toInt()
        
        // Calculate remaining days
        val remainingDays = kotlin.math.max(0, requiredDays - wfoDays)
        
        // Calculate remaining workdays in month (from today to end of month)
        val today = LocalDate.now()
        val remainingWorkdays = if (yearMonth == YearMonth.from(today)) {
            WorkdayCalculator.getRemainingWorkdays(yearMonth, today)
        } else {
            0
        }
        
        // Current rate
        val currentRate = if (effectiveWorkdays > 0) wfoDays.toFloat() / effectiveWorkdays else 0f

        return MonthlyStatistics(
            yearMonth = yearMonth,
            totalWorkdays = totalWorkdays,
            effectiveWorkdays = effectiveWorkdays,
            leaveDays = leaveDays,
            wfoDays = wfoDays,
            wfhDays = records.count { it.workMode == WorkMode.WFH },
            requiredDays = requiredDays,
            remainingDays = remainingDays,
            remainingWorkdays = remainingWorkdays,
            currentRate = currentRate
        )
    }

    suspend fun getAllMonthlyStatistics(): List<MonthlyStatistics> {
        val currentMonth = YearMonth.now()
        val statistics = mutableListOf<MonthlyStatistics>()
        
        for (i in 0..11) {
            val month = currentMonth.minusMonths(i.toLong())
            statistics.add(getMonthlyStatistics(month))
        }
        
        return statistics.reversed()
    }
}

data class MonthlyStatistics(
    val yearMonth: YearMonth,
    val totalWorkdays: Int,
    val effectiveWorkdays: Int,  // totalWorkdays - leaveDays
    val leaveDays: Int,
    val wfoDays: Int,
    val wfhDays: Int,
    val requiredDays: Int,  // effectiveWorkdays * 0.6
    val remainingDays: Int,  // 还需多少天WFO才能达到目标
    val remainingWorkdays: Int,  // 本月剩余工作日（从今天到月底）
    val currentRate: Float
)
