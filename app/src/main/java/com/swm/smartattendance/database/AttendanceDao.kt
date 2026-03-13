package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.Attendance
import com.swm.smartattendance.model.AttendanceWithStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Transaction
    @Query("""
        SELECT * FROM attendance 
        WHERE date = :date AND subjectId = :subjectId AND classId = :classId
        ORDER BY markedAt DESC
    """)
    fun getAttendanceBySession(
        date: String,
        subjectId: Long,
        classId: Long
    ): Flow<List<AttendanceWithStudent>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC, markedAt DESC")
    fun getAttendanceByStudent(studentId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY markedAt DESC")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("""
        SELECT * FROM attendance 
        WHERE studentId = :studentId AND date = :date AND subjectId = :subjectId
        LIMIT 1
    """)
    suspend fun getAttendanceRecord(
        studentId: Long,
        date: String,
        subjectId: Long
    ): Attendance?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(attendance: List<Attendance>)

    @Delete
    suspend fun deleteAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE id = :attendanceId")
    suspend fun deleteAttendanceById(attendanceId: Long)
}
