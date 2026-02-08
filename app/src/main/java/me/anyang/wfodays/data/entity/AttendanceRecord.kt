package me.anyang.wfodays.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey
    val date: Long,
    val isPresent: Boolean = true,
    val recordType: RecordType = RecordType.AUTO,
    val workMode: WorkMode = WorkMode.WFO,
    val location: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RecordType {
    AUTO,
    MANUAL
}

enum class WorkMode {
    WFO,    // Work From Office - 在公司办公
    WFH,    // Work From Home - 在家办公
    LEAVE   // 休假
}
