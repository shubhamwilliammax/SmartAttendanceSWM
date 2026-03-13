package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.database.SubjectDao
import com.swm.smartattendance.model.Attendance
import com.swm.smartattendance.model.AttendanceMethod
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(
    private val attendanceDao: AttendanceDao,
    private val studentDao: StudentDao,
    private val subjectDao: SubjectDao,
    private val academicClassDao: com.swm.smartattendance.database.AcademicClassDao
) : ViewModel() {

    val classes = academicClassDao.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _selectedClassId = MutableStateFlow(0L)
    val subjects = _selectedClassId.flatMapLatest { subjectDao.getByClassId(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun selectClass(classId: Long) { _selectedClassId.value = classId }

    private val _selectedDate = MutableStateFlow(DateUtils.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _subjectId = MutableStateFlow(0L)
    private val _classId = MutableStateFlow(0L)

    fun setSession(date: String, subjectId: Long, classId: Long) {
        _selectedDate.value = date
        _subjectId.value = subjectId
        _classId.value = classId
    }

    fun setDate(date: String) { _selectedDate.value = date }

    fun getAttendanceForSession(date: String, subjectId: Long, classId: Long) =
        attendanceDao.getAttendanceBySession(date, subjectId, classId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun markAttendance(
        studentId: Long,
        subjectId: Long,
        classId: Long,
        date: String = DateUtils.getCurrentDate(),
        method: AttendanceMethod
    ): Boolean {
        val existing = attendanceDao.getAttendanceRecord(studentId, date, subjectId)
        if (existing != null) return false
        attendanceDao.insertAttendance(
            Attendance(studentId = studentId, subjectId = subjectId, classId = classId, date = date, method = method)
        )
        return true
    }

    suspend fun markAttendanceByBleId(bleId: String, date: String, subjectId: Long, classId: Long): Boolean {
        val student = studentDao.getStudentByBleId(bleId) ?: return false
        return markAttendance(student.id, subjectId, classId, date, AttendanceMethod.BLE)
    }

    suspend fun markAttendanceByMacAddress(macAddress: String, date: String, subjectId: Long, classId: Long): Boolean {
        val student = studentDao.getStudentByMacAddress(macAddress) ?: return false
        return markAttendance(student.id, subjectId, classId, date, AttendanceMethod.WIFI)
    }

    suspend fun markAttendanceByRollNumber(rollNumber: String, date: String, subjectId: Long, classId: Long): Boolean {
        val student = studentDao.getStudentByRollNumber(rollNumber) ?: return false
        return markAttendance(student.id, subjectId, classId, date, AttendanceMethod.QR)
    }

    suspend fun markAttendanceByFaceId(faceId: String, date: String, subjectId: Long, classId: Long): Boolean {
        val student = studentDao.getStudentByFaceId(faceId) ?: return false
        return markAttendance(student.id, subjectId, classId, date, AttendanceMethod.FACE)
    }

    class Factory(
        private val attendanceDao: AttendanceDao,
        private val studentDao: StudentDao,
        private val subjectDao: SubjectDao,
        private val academicClassDao: com.swm.smartattendance.database.AcademicClassDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AttendanceViewModel(attendanceDao, studentDao, subjectDao, academicClassDao) as T
    }
}
