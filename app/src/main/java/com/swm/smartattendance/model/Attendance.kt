package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an attendance record.
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId", "date", "subjectId"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val classId: Long,
    val date: String, // dd/MM/yyyy
    val markedAt: Long = System.currentTimeMillis(),
    val method: AttendanceMethod = AttendanceMethod.QR
)

enum class AttendanceMethod {
    BLE, WIFI, QR, IMPORT
}
