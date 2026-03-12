package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.model.Student
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Student Manager screen.
 * Handles CRUD operations for students.
 */
class StudentViewModel(private val studentDao: StudentDao) : ViewModel() {

    val students: StateFlow<List<Student>> = studentDao.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addStudent(
        name: String,
        rollNumber: String,
        macAddress: String? = null,
        bleId: String? = null,
        faceId: String? = null,
        className: String? = null
    ) {
        viewModelScope.launch {
            studentDao.insertStudent(
                Student(
                    name = name,
                    rollNumber = rollNumber,
                    macAddress = macAddress?.takeIf { it.isNotBlank() },
                    bleId = bleId?.takeIf { it.isNotBlank() },
                    faceId = faceId?.takeIf { it.isNotBlank() },
                    className = className?.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            studentDao.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            studentDao.deleteStudent(student)
        }
    }

    class Factory(private val studentDao: StudentDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentViewModel(studentDao) as T
        }
    }
}
