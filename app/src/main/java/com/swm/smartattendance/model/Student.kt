package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a student in the attendance system.
 */
@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = AcademicClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId", "rollNumber"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val name: String,
    val rollNumber: String,
    val macAddress: String? = null,
    val bleId: String? = null,
    val totalClasses: Int = 0,
    val totalPresent: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
