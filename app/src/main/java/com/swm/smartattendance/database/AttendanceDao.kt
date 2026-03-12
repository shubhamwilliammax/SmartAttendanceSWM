package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.Attendance
import com.swm.smartattendance.model.AttendanceWithStudent
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Attendance entity.
 * Handles all database operations for attendance records.
 */
@Dao
interface AttendanceDao {

    @Transaction
    @Query("""
        SELECT * FROM attendance 
        WHERE date = :date AND subjectName = :subjectName AND className = :className
        ORDER BY markedAt DESC
    """)
    fun getAttendanceBySession(
        date: String,
        subjectName: String,
        className: String
    ): Flow<List<AttendanceWithStudent>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC, markedAt DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY markedAt DESC")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId AND date = :date AND subjectName = :subjectName AND className = :className
        LIMIT 1
    """)
    suspend fun getAttendanceRecord(
        studentId: Long,
        date: String,
        subjectName: String,
        className: String
    ): Attendance?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE id = :attendanceId")
    suspend fun deleteAttendanceById(attendanceId: Long)
}
