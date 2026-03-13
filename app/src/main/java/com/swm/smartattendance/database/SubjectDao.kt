package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE classId = :classId ORDER BY name")
    fun getByClassId(classId: Long): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Query("SELECT * FROM subjects WHERE classId = :classId AND code = :code LIMIT 1")
    suspend fun getByCode(classId: Long, code: String): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: Subject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<Subject>)

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)
}
