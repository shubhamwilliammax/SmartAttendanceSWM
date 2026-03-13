package com.swm.smartattendance.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Combined entity for attendance with student and subject details.
 */
data class AttendanceWithStudent(
    @Embedded val attendance: Attendance,
    @Relation(parentColumn = "studentId", entityColumn = "id")
    val student: Student,
    @Relation(parentColumn = "subjectId", entityColumn = "id")
    val subject: Subject,
    @Relation(parentColumn = "classId", entityColumn = "id")
    val academicClass: AcademicClass
)
