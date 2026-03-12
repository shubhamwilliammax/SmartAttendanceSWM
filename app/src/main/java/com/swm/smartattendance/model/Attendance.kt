package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an attendance record.
 * Links student to a specific date, subject, and class.
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId", "date", "subjectName", "className"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // Format: dd/MM/yyyy
    val subjectName: String,
    val className: String,
    val markedAt: Long = System.currentTimeMillis(),
    val method: AttendanceMethod = AttendanceMethod.FACE
)

/**
 * Enum for attendance marking method
 */
enum class AttendanceMethod {
    FACE,
    BLE,
    WIFI,
    QR
}
