package com.swm.smartattendance.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for custom short forms (subjects, branches)
 */
@Entity(tableName = "short_forms", indices = [Index(value = ["fullName", "type"], unique = true)])
data class ShortForm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val shortForm: String,
    val type: ShortFormType,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ShortFormType {
    SUBJECT,
    BRANCH
}
