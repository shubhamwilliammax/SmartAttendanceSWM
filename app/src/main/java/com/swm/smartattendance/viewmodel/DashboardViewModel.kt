package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Dashboard screen.
 * Provides overview statistics.
 */
class DashboardViewModel(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) : ViewModel() {

    val studentCount: StateFlow<Int> = studentDao.getAllStudents()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _todayAttendanceCount = MutableStateFlow(0)
    val todayAttendanceCount: StateFlow<Int> = _todayAttendanceCount.asStateFlow()

    init {
        viewModelScope.launch {
            attendanceDao.getAttendanceByDate(DateUtils.getCurrentDate()).collect { list ->
                _todayAttendanceCount.value = list.distinctBy { it.studentId }.size
            }
        }
    }

    class Factory(
        private val studentDao: StudentDao,
        private val attendanceDao: AttendanceDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(studentDao, attendanceDao) as T
        }
    }
}
