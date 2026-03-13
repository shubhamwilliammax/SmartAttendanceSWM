package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an academic class (e.g., B.Tech CSE Semester VI)
 */
@Entity(tableName = "academic_classes")
data class AcademicClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val branch: String,
    val semester: Int,
    val session: String, // e.g., 2025-2026
    val createdAt: Long = System.currentTimeMillis()
)
