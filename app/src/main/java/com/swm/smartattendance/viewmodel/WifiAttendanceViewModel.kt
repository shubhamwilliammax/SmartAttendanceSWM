package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.model.AttendanceMethod
import com.swm.smartattendance.model.Student
import com.swm.smartattendance.model.WifiAttendanceRequest
import com.swm.smartattendance.wifi.WifiServerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiAttendanceViewModel(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val attendanceViewModel: AttendanceViewModel
) : ViewModel() {

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _receivedRequests = MutableStateFlow<List<WifiAttendanceRequest>>(emptyList())
    val receivedRequests: StateFlow<List<WifiAttendanceRequest>> = _receivedRequests.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _knownStudents = MutableStateFlow<List<Student>>(emptyList())
    val knownStudents: StateFlow<List<Student>> = _knownStudents.asStateFlow()

    private var serverManager: WifiServerManager? = null

    private var currentSubjectId: Long = 0
    private var currentClassId: Long = 0
    private var currentDate: String = ""

    fun startServer(date: String, subjectId: Long, classId: Long) {
        if (_isServerRunning.value) return
        
        currentDate = date
        currentSubjectId = subjectId
        currentClassId = classId

        val manager = WifiServerManager(8080)
        serverManager = manager
        try {
            manager.start()
            _isServerRunning.value = true
            _error.value = null
            
            viewModelScope.launch {
                manager.attendanceFlow.collect { request ->
                    processAttendanceRequest(request)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Failed to start server: ${e.localizedMessage}"
            _isServerRunning.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun stopServer() {
        serverManager?.stop()
        serverManager = null
        _isServerRunning.value = false
    }

    private fun processAttendanceRequest(request: WifiAttendanceRequest) {
        val currentList = _receivedRequests.value.toMutableList()
        if (currentList.none { it.studentId == request.studentId }) {
            currentList.add(request)
            _receivedRequests.value = currentList
            
            viewModelScope.launch {
                val student = studentDao.getStudentByRollNumber(request.studentId)
                if (student != null) {
                    val knownList = _knownStudents.value.toMutableList()
                    if (knownList.none { it.id == student.id }) {
                        knownList.add(student)
                        _knownStudents.value = knownList
                        
                        // Automatically mark attendance in DB
                        attendanceViewModel.markAttendance(
                            studentId = student.id,
                            subjectId = currentSubjectId,
                            classId = currentClassId,
                            date = currentDate,
                            method = AttendanceMethod.WIFI
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }

    class Factory(
        private val studentDao: StudentDao,
        private val attendanceDao: AttendanceDao,
        private val attendanceViewModel: AttendanceViewModel
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WifiAttendanceViewModel(studentDao, attendanceDao, attendanceViewModel) as T
    }
}
