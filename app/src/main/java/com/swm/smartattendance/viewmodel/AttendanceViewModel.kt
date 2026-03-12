package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.database.StudentDao
import com.swm.smartattendance.model.Attendance
import com.swm.smartattendance.model.AttendanceMethod
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for attendance operations.
 * Handles marking attendance and retrieving records.
 */
class AttendanceViewModel(
    private val attendanceDao: AttendanceDao,
    private val studentDao: StudentDao
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _subjectName = MutableStateFlow("")
    val subjectName: StateFlow<String> = _subjectName.asStateFlow()

    private val _className = MutableStateFlow("")
    val className: StateFlow<String> = _className.asStateFlow()

    fun setSession(date: String, subjectName: String, className: String) {
        _selectedDate.value = date
        _subjectName.value = subjectName
        _className.value = className
    }

    fun setDate(date: String) { _selectedDate.value = date }
    fun setSubject(name: String) { _subjectName.value = name }
    fun setClassName(name: String) { _className.value = name }

    val attendanceForSession: StateFlow<List<AttendanceWithStudent>> =
        attendanceDao.getAttendanceBySession(
            _selectedDate.value,
            _subjectName.value,
            _className.value
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAttendanceForSession(date: String, subject: String, className: String) =
        attendanceDao.getAttendanceBySession(date, subject, className)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Mark attendance for a student
     */
    suspend fun markAttendance(
        studentId: Long,
        date: String = DateUtils.getCurrentDate(),
        subjectName: String,
        className: String,
        method: AttendanceMethod
    ): Boolean {
        val existing = attendanceDao.getAttendanceRecord(studentId, date, subjectName, className)
        if (existing != null) return false // Already marked

        attendanceDao.insertAttendance(
            Attendance(
                studentId = studentId,
                date = date,
                subjectName = subjectName,
                className = className,
                method = method
            )
        )
        return true
    }

    /**
     * Mark attendance by BLE ID
     */
    suspend fun markAttendanceByBleId(
        bleId: String,
        date: String,
        subjectName: String,
        className: String
    ): Boolean {
        val student = studentDao.getStudentByBleId(bleId) ?: return false
        return markAttendance(student.id, date, subjectName, className, AttendanceMethod.BLE)
    }

    /**
     * Mark attendance by MAC address
     */
    suspend fun markAttendanceByMacAddress(
        macAddress: String,
        date: String,
        subjectName: String,
        className: String
    ): Boolean {
        val student = studentDao.getStudentByMacAddress(macAddress) ?: return false
        return markAttendance(student.id, date, subjectName, className, AttendanceMethod.WIFI)
    }

    /**
     * Mark attendance by Roll Number (for QR scan flow)
     */
    suspend fun markAttendanceByRollNumber(
        rollNumber: String,
        date: String,
        subjectName: String,
        className: String
    ): Boolean {
        val student = studentDao.getStudentByRollNumber(rollNumber) ?: return false
        return markAttendance(student.id, date, subjectName, className, AttendanceMethod.QR)
    }

    /**
     * Mark attendance by Face ID
     */
    suspend fun markAttendanceByFaceId(
        faceId: String,
        date: String,
        subjectName: String,
        className: String
    ): Boolean {
        val student = studentDao.getStudentByFaceId(faceId) ?: return false
        return markAttendance(student.id, date, subjectName, className, AttendanceMethod.FACE)
    }

    class Factory(
        private val attendanceDao: AttendanceDao,
        private val studentDao: StudentDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AttendanceViewModel(attendanceDao, studentDao) as T
        }
    }
}
