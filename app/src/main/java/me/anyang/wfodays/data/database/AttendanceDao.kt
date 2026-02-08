package me.anyang.wfodays.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.anyang.wfodays.data.entity.AttendanceRecord

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getRecordByDate(date: Long): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteRecordByDate(date: Long)

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date BETWEEN :startDate AND :endDate AND isPresent = 1")
    suspend fun countPresentDays(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM attendance_records WHERE date BETWEEN :startDate AND :endDate AND isPresent = 1 ORDER BY date DESC")
    suspend fun getPresentRecordsBetween(startDate: Long, endDate: Long): List<AttendanceRecord>
}
