package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a class routine schedule.
 * Used for routine detection based on current time.
 */
@Entity(tableName = "class_routine")
data class ClassRoutine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1=Sunday, 2=Monday, ..., 7=Saturday
    val startTime: String, // Format: HH:mm
    val endTime: String,
    val subjectName: String,
    val className: String,
    val createdAt: Long = System.currentTimeMillis()
)
