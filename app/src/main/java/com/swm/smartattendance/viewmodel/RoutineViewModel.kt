package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AcademicClassDao
import com.swm.smartattendance.database.RoutineSlotDao
import com.swm.smartattendance.database.SubjectDao
import com.swm.smartattendance.model.RoutineSlot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutineViewModel(
    private val routineSlotDao: RoutineSlotDao,
    private val academicClassDao: AcademicClassDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    fun getRoutinesByClass(classId: Long) =
        routineSlotDao.getByClassId(classId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRoutineSlot(slot: RoutineSlot) {
        viewModelScope.launch { routineSlotDao.insert(slot) }
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
