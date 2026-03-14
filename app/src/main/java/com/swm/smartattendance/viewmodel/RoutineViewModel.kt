package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AcademicClassDao
import com.swm.smartattendance.database.RoutineSlotDao
import com.swm.smartattendance.database.SubjectDao
import com.swm.smartattendance.model.AcademicClass
import com.swm.smartattendance.model.RoutineSlot
import com.swm.smartattendance.model.Subject
import com.swm.smartattendance.parser.RoutineParser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class RoutineViewModel(
    private val routineSlotDao: RoutineSlotDao,
    private val academicClassDao: AcademicClassDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    fun getRoutinesByClass(classId: Long) =
        routineSlotDao.getByClassId(classId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun importRoutine(parsed: RoutineParser.ParsedRoutine) {
        // 1. Create Class
        val classId = academicClassDao.insert(
            AcademicClass(
                name = parsed.className,
                branch = parsed.branch,
                semester = parsed.semester,
                session = parsed.session
            )
        )

        // 2. Create Subjects and map names to IDs
        val subjectMap = mutableMapOf<String, Long>()
        parsed.subjects.forEach { ps ->
            val id = subjectDao.insert(
                Subject(
                    classId = classId,
                    name = ps.name,
                    code = ps.code,
                    shortForm = ps.shortForm
                )
            )
            subjectMap[ps.name] = id
        }

        // 3. Create Routine Slots
        parsed.schedule.forEach { slot ->
            val subId = subjectMap[slot.subjectName]
            if (subId != null) {
                routineSlotDao.insert(
                    RoutineSlot(
                        classId = classId,
                        subjectId = subId,
                        dayOfWeek = slot.dayOfWeek,
                        startTime = slot.startTime,
                        endTime = slot.endTime
                    )
                )
            }
        }
    }

    /**
     * Suggests a subject based on current time and class
     */
    suspend fun suggestSubject(classId: Long): Long? {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentTime = String.format("%02d:%02d", hour, minute)

        // Simple check: current time between start and end
        // This is a placeholder, real logic would iterate through slots
        return null
    }

    class Factory(
        private val routineSlotDao: RoutineSlotDao,
        private val academicClassDao: AcademicClassDao,
        private val subjectDao: SubjectDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RoutineViewModel(routineSlotDao, academicClassDao, subjectDao) as T
    }
}
