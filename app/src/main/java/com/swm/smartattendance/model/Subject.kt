package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a subject
 */
@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = AcademicClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["facultyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["classId", "code"], unique = true)]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val name: String,
    val code: String,
    val shortForm: String,
    val facultyId: Long? = null,
    val venue: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
