package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY rollNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNumber ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Long): Student?

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    suspend fun getStudentByRollNumber(rollNumber: String): Student?

    @Query("SELECT * FROM students WHERE classId = :classId AND rollNumber = :rollNumber LIMIT 1")
    suspend fun getStudentByClassAndRoll(classId: Long, rollNumber: String): Student?

    @Query("SELECT * FROM students WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getStudentByMacAddress(macAddress: String): Student?

    @Query("SELECT * FROM students WHERE bleId = :bleId LIMIT 1")
    suspend fun getStudentByBleId(bleId: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Long)
}
