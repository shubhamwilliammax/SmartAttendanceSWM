package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for routine schedule slots (day, time, subject)
 */
@Entity(
    tableName = "routine_slots",
    foreignKeys = [
        ForeignKey(
            entity = AcademicClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId", "dayOfWeek", "startTime"], unique = true)]
)
data class RoutineSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val subjectId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val venue: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
