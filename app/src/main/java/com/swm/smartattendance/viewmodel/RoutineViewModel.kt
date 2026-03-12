package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.ClassRoutineDao
import com.swm.smartattendance.model.ClassRoutine
import com.swm.smartattendance.utils.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Routine Manager screen.
 * Handles class routine and auto-detection based on time.
 */
class RoutineViewModel(private val routineDao: ClassRoutineDao) : ViewModel() {

    val routines: StateFlow<List<ClassRoutine>> = routineDao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRoutine(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subjectName: String,
        className: String
    ) {
        viewModelScope.launch {
            routineDao.insertRoutine(
                ClassRoutine(
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    subjectName = subjectName,
                    className = className
                )
            )
        }
    }

    fun updateRoutine(routine: ClassRoutine) {
        viewModelScope.launch {
            routineDao.updateRoutine(routine)
        }
    }

    fun deleteRoutine(routine: ClassRoutine) {
        viewModelScope.launch {
            routineDao.deleteRoutine(routine)
        }
    }

    /**
     * Get current subject based on time and day
     */
    fun getCurrentSubject(): ClassRoutine? {
        var result: ClassRoutine? = null
        viewModelScope.launch {
            val day = DateUtils.getCurrentDayOfWeek()
            val time = DateUtils.getCurrentTime()
            routineDao.getRoutinesByDay(day).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                emptyList()
            ).value.firstOrNull { routine ->
                DateUtils.isTimeInRange(time, routine.startTime, routine.endTime)
            }?.let { result = it }
        }
        return result
    }

    class Factory(private val routineDao: ClassRoutineDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoutineViewModel(routineDao) as T
        }
    }
}
