package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.RoutineSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineSlotDao {
    @Query("SELECT * FROM routine_slots WHERE classId = :classId ORDER BY dayOfWeek, startTime")
    fun getByClassId(classId: Long): Flow<List<RoutineSlot>>

    @Query("SELECT * FROM routine_slots WHERE classId = :classId AND dayOfWeek = :day ORDER BY startTime")
    fun getByClassAndDay(classId: Long, day: Int): Flow<List<RoutineSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: RoutineSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(slots: List<RoutineSlot>)

    @Query("DELETE FROM routine_slots WHERE classId = :classId")
    suspend fun deleteByClassId(classId: Long)
}
