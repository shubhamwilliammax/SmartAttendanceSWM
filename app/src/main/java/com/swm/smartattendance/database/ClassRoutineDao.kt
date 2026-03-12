package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.ClassRoutine
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ClassRoutine entity.
 * Handles routine schedule operations.
 */
@Dao
interface ClassRoutineDao {

    @Query("SELECT * FROM class_routine ORDER BY dayOfWeek, startTime")
    fun getAllRoutines(): Flow<List<ClassRoutine>>

    @Query("SELECT * FROM class_routine WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getRoutinesByDay(dayOfWeek: Int): Flow<List<ClassRoutine>>

    @Query("SELECT * FROM class_routine WHERE id = :routineId")
    suspend fun getRoutineById(routineId: Long): ClassRoutine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: ClassRoutine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<ClassRoutine>)

    @Update
    suspend fun updateRoutine(routine: ClassRoutine)

    @Delete
    suspend fun deleteRoutine(routine: ClassRoutine)

    @Query("DELETE FROM class_routine")
    suspend fun deleteAllRoutines()
}
