package com.swm.smartattendance.database

import androidx.room.*
import com.swm.smartattendance.model.ShortForm
import com.swm.smartattendance.model.ShortFormType

@Dao
interface ShortFormDao {
    @Query("SELECT * FROM short_forms WHERE fullName = :fullName AND type = :type LIMIT 1")
    suspend fun get(fullName: String, type: ShortFormType): ShortForm?

    @Query("SELECT * FROM short_forms WHERE type = :type")
    suspend fun getByType(type: ShortFormType): List<ShortForm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortForm: ShortForm): Long

    @Update
    suspend fun update(shortForm: ShortForm)

    @Delete
    suspend fun delete(shortForm: ShortForm)
}
