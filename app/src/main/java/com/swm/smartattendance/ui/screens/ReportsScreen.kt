package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.ReportsViewModel
import java.io.File

/**
 * Reports screen - View and export attendance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    attendanceViewModel: AttendanceViewModel,
    reportsViewModel: ReportsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var subjectName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }

    val attendanceFlow = attendanceViewModel.getAttendanceForSession(date, subjectName, className)
    val attendance by attendanceFlow.collectAsState(initial = emptyList())
    val exportStatus by reportsViewModel.exportStatus.collectAsState()

    LaunchedEffect(exportStatus) {
        if (exportStatus is ReportsViewModel.ExportStatus.Success) {
            reportsViewModel.resetExportStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (dd/MM/yyyy)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val dir = File(context.getExternalFilesDir(null), "exports")
                                dir.mkdirs()
                                val file = File(dir, "attendance_${date.replace("/", "_")}.pdf")
                                reportsViewModel.exportToPdf(file, date, subjectName, className)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                        OutlinedButton(
                            onClick = {
                                val dir = File(context.getExternalFilesDir(null), "exports")
                                dir.mkdirs()
                                val file = File(dir, "attendance_${date.replace("/", "_")}.xlsx")
                                reportsViewModel.exportToExcel(file, date, subjectName, className)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel")
                        }
                    }
                }
            }

            when (val status = exportStatus) {
                is ReportsViewModel.ExportStatus.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
                is ReportsViewModel.ExportStatus.Success -> {
                    Text("Exported to ${status.filePath}", modifier = Modifier.padding(16.dp))
                }
                is ReportsViewModel.ExportStatus.Error -> {
                    Text("Error: ${status.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                else -> {}
            }

            Text(
                "Attendance Records",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attendance, key = { it.attendance.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.student.name, style = MaterialTheme.typography.titleSmall)
                                Text("${item.student.rollNumber} • ${item.attendance.method}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(DateUtils.formatDateTime(item.attendance.markedAt), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
