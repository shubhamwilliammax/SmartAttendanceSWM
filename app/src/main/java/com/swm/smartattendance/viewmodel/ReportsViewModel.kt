package com.swm.smartattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swm.smartattendance.database.AttendanceDao
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.utils.ExportUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for Reports screen.
 * Handles attendance export to PDF and Excel.
 */
class ReportsViewModel(private val attendanceDao: AttendanceDao) : ViewModel() {

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()

    fun exportToPdf(
        outputFile: File,
        date: String,
        subjectName: String,
        className: String,
        title: String = "Attendance Report"
    ) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            try {
                val attendance = attendanceDao.getAttendanceBySession(date, subjectName, className)
                    .first()
                val result = ExportUtils.exportToPdf(attendance, outputFile, title)
                _exportStatus.value = if (result.isSuccess) {
                    ExportStatus.Success(result.getOrNull()!!.absolutePath)
                } else {
                    ExportStatus.Error(result.exceptionOrNull()?.message ?: "Export failed")
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun exportToExcel(
        outputFile: File,
        date: String,
        subjectName: String,
        className: String,
        sheetName: String = "Attendance"
    ) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            try {
                val attendance = attendanceDao.getAttendanceBySession(date, subjectName, className)
                    .first()
                val result = ExportUtils.exportToExcel(attendance, outputFile, sheetName)
                _exportStatus.value = if (result.isSuccess) {
                    ExportStatus.Success(result.getOrNull()!!.absolutePath)
                } else {
                    ExportStatus.Error(result.exceptionOrNull()?.message ?: "Export failed")
                }
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }

    sealed class ExportStatus {
        object Idle : ExportStatus()
        object Loading : ExportStatus()
        data class Success(val filePath: String) : ExportStatus()
        data class Error(val message: String) : ExportStatus()
    }

    class Factory(private val attendanceDao: AttendanceDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(attendanceDao) as T
        }
    }
}
