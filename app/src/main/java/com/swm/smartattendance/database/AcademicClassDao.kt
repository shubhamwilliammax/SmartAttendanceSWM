package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.AcademicClass
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicClassDao {
    @Query("SELECT * FROM academic_classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<AcademicClass>>

    @Query("SELECT * FROM academic_classes WHERE id = :id")
    suspend fun getById(id: Long): AcademicClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cls: AcademicClass): Long

    @Update
    suspend fun update(cls: AcademicClass)

    @Delete
    suspend fun delete(cls: AcademicClass)
}
