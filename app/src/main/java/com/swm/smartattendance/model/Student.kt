package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a student in the attendance system.
 * Contains all identifiers for different attendance methods.
 */
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val macAddress: String? = null,
    val bleId: String? = null,
    val faceId: String? = null,
    val className: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
