package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.ReportsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    attendanceViewModel: AttendanceViewModel,
    reportsViewModel: ReportsViewModel,
    studentViewModel: com.swm.smartattendance.viewmodel.StudentViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var date by remember { mutableStateOf(DateUtils.getCurrentDate()) }
    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }

    val classes by studentViewModel.classes.collectAsState()
    val subjects by reportsViewModel.getSubjectsByClass(selectedClassId).collectAsState()

    LaunchedEffect(classes) {
        if (selectedClassId == 0L && classes.isNotEmpty()) selectedClassId = classes.first().id
    }
    LaunchedEffect(subjects) {
        if (selectedSubjectId == 0L && subjects.isNotEmpty()) selectedSubjectId = subjects.first().id
    }

    val attendanceFlow = attendanceViewModel.getAttendanceForSession(
        date,
        selectedSubjectId.takeIf { it > 0 } ?: 0L,
        selectedClassId.takeIf { it > 0 } ?: 0L
    )
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
                    Text("Class")
                    classes.forEach { cls ->
                        FilterChip(
                            selected = selectedClassId == cls.id,
                            onClick = { selectedClassId = cls.id; selectedSubjectId = 0L },
                            label = { Text(cls.name) }
                        )
                    }
                    if (subjects.isNotEmpty()) {
                        Text("Subject")
                        subjects.forEach { subj ->
                            FilterChip(
                                selected = selectedSubjectId == subj.id,
                                onClick = { selectedSubjectId = subj.id },
                                label = { Text(subj.name) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (selectedSubjectId > 0 && selectedClassId > 0) {
                                    val dir = File(context.getExternalFilesDir(null), "exports")
                                    dir.mkdirs()
                                    val file = File(dir, "attendance_${date.replace("/", "_")}.pdf")
                                    reportsViewModel.exportToPdf(file, date, selectedSubjectId, selectedClassId)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF")
                        }
                        OutlinedButton(
                            onClick = {
                                if (selectedSubjectId > 0 && selectedClassId > 0) {
                                    val dir = File(context.getExternalFilesDir(null), "exports")
                                    dir.mkdirs()
                                    val file = File(dir, "attendance_${date.replace("/", "_")}.xlsx")
                                    reportsViewModel.exportToExcel(file, date, selectedSubjectId, selectedClassId)
                                }
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
