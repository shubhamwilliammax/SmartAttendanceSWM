package com.swm.smartattendance.ui.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.utils.ExportUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    attendanceViewModel: AttendanceViewModel,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf(1) } // 1: One Day, 2: Date Range
    var startDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var endDate by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var selectedClassId by remember { mutableStateOf(0L) }
    var exportFormat by remember { mutableStateOf("Excel") }

    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()

    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == 0L) {
            selectedClassId = classes.first().id
            attendanceViewModel.selectClass(classes.first().id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Attendance") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Export Options", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedOption == 1, onClick = { selectedOption = 1 })
                Text("One Day")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = selectedOption == 2, onClick = { selectedOption = 2 })
                Text("Date Range")
            }

            if (selectedOption == 1) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Date (dd/mm/yyyy)") },
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("Class Filter", style = MaterialTheme.typography.titleSmall)
            var classExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { classExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(classes.find { it.id == selectedClassId }?.name ?: "Select Class")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                    classes.forEach { cls ->
                        DropdownMenuItem(text = { Text(cls.name) }, onClick = { 
                            selectedClassId = cls.id
                            attendanceViewModel.selectClass(cls.id)
                            classExpanded = false 
                        })
                    }
                }
            }

            Text("Subject Filter", style = MaterialTheme.typography.titleSmall)
            var subExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { subExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(subjects.find { it.id == selectedSubjectId }?.name ?: "All Subjects")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                    DropdownMenuItem(text = { Text("All Subjects") }, onClick = { selectedSubjectId = 0L; subExpanded = false })
                    subjects.forEach { subj ->
                        DropdownMenuItem(text = { Text(subj.name) }, onClick = { selectedSubjectId = subj.id; subExpanded = false })
                    }
                }
            }

            Text("Export Format", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Excel", "CSV", "PDF", "TEXT").forEach { format ->
                    FilterChip(
                        selected = exportFormat == format,
                        onClick = { exportFormat = format },
                        label = { Text(format) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        val attendance = attendanceViewModel.getAttendanceForSession(startDate, selectedSubjectId, selectedClassId).first()
                        if (attendance.isEmpty()) {
                            Toast.makeText(context, "No attendance found for selected filters", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val fileName = "Attendance_${startDate.replace("/", "-")}_${System.currentTimeMillis()}"
                        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        val file = when (exportFormat) {
                            "Excel" -> File(dir, "$fileName.xlsx")
                            "CSV" -> File(dir, "$fileName.csv")
                            "PDF" -> File(dir, "$fileName.pdf")
                            else -> File(dir, "$fileName.txt")
                        }

                        val result = when (exportFormat) {
                            "Excel" -> ExportUtils.exportToExcel(attendance, file)
                            "CSV" -> ExportUtils.exportToCsv(attendance, file)
                            "PDF" -> ExportUtils.exportToPdf(attendance, file)
                            else -> ExportUtils.exportToText(attendance, file)
                        }

                        if (result.isSuccess) {
                            Toast.makeText(context, "Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download Attendance")
            }
        }
    }
}
