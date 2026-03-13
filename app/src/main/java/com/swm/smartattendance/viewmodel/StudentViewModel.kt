package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AcademicClassDao
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.model.Student
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(
    private val studentDao: StudentDao,
    private val academicClassDao: AcademicClassDao
) : ViewModel() {

    val students: StateFlow<List<Student>> = studentDao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes = academicClassDao.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getStudentsByClass(classId: Long) = studentDao.getStudentsByClass(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getDefaultClassId(): Long = academicClassDao.getAllClasses().first().firstOrNull()?.id ?: 1L

    fun addStudent(student: Student) {
        viewModelScope.launch { studentDao.insertStudent(student) }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch { studentDao.updateStudent(student) }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch { studentDao.deleteStudent(student) }
    }

    class Factory(
        private val studentDao: StudentDao,
        private val academicClassDao: AcademicClassDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = StudentViewModel(studentDao, academicClassDao) as T
    }
}
