package com.swm.smartattendance.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Combined entity for attendance with student details.
 * Used for displaying attendance records with student information.
 */
data class AttendanceWithStudent(
    @Embedded val attendance: Attendance,
    @Relation(
        parentColumn = "studentId",
        entityColumn = "id"
    )
    val student: Student
)
