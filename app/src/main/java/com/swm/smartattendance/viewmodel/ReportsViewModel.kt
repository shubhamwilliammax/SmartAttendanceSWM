package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.database.SubjectDao
import com.swm.smartattendance.utils.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.utils.DateUtils
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.io.File

class ReportsViewModel(
    private val attendanceDao: AttendanceDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _selectedClassId = MutableStateFlow(0L)
    val selectedClassId: StateFlow<Long> = _selectedClassId.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow(0L)
    val selectedSubjectId: StateFlow<Long> = _selectedSubjectId.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val subjects = _selectedClassId.flatMapLatest { classId ->
        subjectDao.getByClassId(classId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceList = combine(_selectedDate, _selectedSubjectId, _selectedClassId) { date, subId, classId ->
        Triple(date, subId, classId)
    }.flatMapLatest { (date, subId, classId) ->
        attendanceDao.getAttendanceBySession(date, subId, classId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectClass(classId: Long) {
        if (_selectedClassId.value != classId) {
            _selectedClassId.value = classId
            _selectedSubjectId.value = 0L // Reset subject when class changes
        }
    }

    fun selectSubject(subjectId: Long) {
        _selectedSubjectId.value = subjectId
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()

    fun exportToPdf(outputFile: File, title: String = "Attendance Report") {
        viewModelScope.launch {
            val date = _selectedDate.value
            val subId = _selectedSubjectId.value
            val classId = _selectedClassId.value
            if (subId <= 0 || classId <= 0) return@launch
            
            _exportStatus.value = ExportStatus.Loading
            try {
                val attendance = attendanceDao.getAttendanceBySession(date, subId, classId).first()
                val result = ExportUtils.exportToPdf(attendance, outputFile, title)
                _exportStatus.value = if (result.isSuccess) ExportStatus.Success(result.getOrNull()!!.absolutePath)
                else ExportStatus.Error(result.exceptionOrNull()?.message ?: "Export failed")
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportToExcel(outputFile: File, sheetName: String = "Attendance") {
        viewModelScope.launch {
            val date = _selectedDate.value
            val subId = _selectedSubjectId.value
            val classId = _selectedClassId.value
            if (subId <= 0 || classId <= 0) return@launch

            _exportStatus.value = ExportStatus.Loading
            try {
                val attendance = attendanceDao.getAttendanceBySession(date, subId, classId).first()
                val result = ExportUtils.exportToExcel(attendance, outputFile, sheetName)
                _exportStatus.value = if (result.isSuccess) ExportStatus.Success(result.getOrNull()!!.absolutePath)
                else ExportStatus.Error(result.exceptionOrNull()?.message ?: "Export failed")
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetExportStatus() { _exportStatus.value = ExportStatus.Idle }

    sealed class ExportStatus {
        data object Idle : ExportStatus()
        data object Loading : ExportStatus()
        data class Success(val filePath: String) : ExportStatus()
        data class Error(val message: String) : ExportStatus()
    }

    class Factory(
        private val attendanceDao: AttendanceDao,
        private val subjectDao: SubjectDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportsViewModel(attendanceDao, subjectDao) as T
    }
}
