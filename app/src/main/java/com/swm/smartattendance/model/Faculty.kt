package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a faculty member
 */
@Entity(tableName = "faculty")
data class Faculty(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortName: String,
    val createdAt: Long = System.currentTimeMillis()
)
