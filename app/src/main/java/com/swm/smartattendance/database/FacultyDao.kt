package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.Faculty
import kotlinx.coroutines.flow.Flow

@Dao
interface FacultyDao {
    @Query("SELECT * FROM faculty ORDER BY shortName")
    fun getAll(): Flow<List<Faculty>>

    @Query("SELECT * FROM faculty WHERE id = :id")
    suspend fun getById(id: Long): Faculty?

    @Query("SELECT * FROM faculty WHERE shortName = :shortName LIMIT 1")
    suspend fun getByShortName(shortName: String): Faculty?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(faculty: Faculty): Long

    @Update
    suspend fun update(faculty: Faculty)

    @Delete
    suspend fun delete(faculty: Faculty)
}
