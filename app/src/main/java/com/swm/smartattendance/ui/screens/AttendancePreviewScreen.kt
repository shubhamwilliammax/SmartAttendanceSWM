package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.model.AttendanceWithStudent
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendancePreviewScreen(
    date: String,
    subjectId: Long,
    classId: Long,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val attendanceList by attendanceViewModel.getAttendanceForSession(date, subjectId, classId).collectAsState(emptyList())
    var showDownloadDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
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
            if (attendanceList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance records for this session.")
                }
            } else {
                Text(
                    "Total Students: ${attendanceList.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text("Roll No", Modifier.weight(1.2f), style = MaterialTheme.typography.labelLarge)
                            Text("Name", Modifier.weight(2f), style = MaterialTheme.typography.labelLarge)
                            Text("Time", Modifier.weight(0.8f), style = MaterialTheme.typography.labelLarge)
                        }
                        HorizontalDivider()
                    }
                    items(attendanceList) { item ->
                        AttendanceRow(item)
                    }
                }
            }
        }
    }

    if (showDownloadDialog) {
        var exportOption by remember { mutableStateOf(3) } // 1: Roll, 2: Name, 3: Both
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = { Text("Download Attendance") },
            text = {
                Column {
                    Text("Select format for .txt file:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exportOption == 1, onClick = { exportOption = 1 })
                        Text("Roll No only")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exportOption == 2, onClick = { exportOption = 2 })
                        Text("Name only")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = exportOption == 3, onClick = { exportOption = 3 })
                        Text("Roll No + Name")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    saveAttendanceToFile(context, attendanceList, exportOption)
                    showDownloadDialog = false
                }) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttendanceRow(item: AttendanceWithStudent) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.student.rollNumber, Modifier.weight(1.2f), style = MaterialTheme.typography.bodyMedium)
        Text(item.student.name, Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.attendance.markedAt))
        Text(time, Modifier.weight(0.8f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun saveAttendanceToFile(context: android.content.Context, list: List<AttendanceWithStudent>, option: Int) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Attendance_$timestamp.txt"
    val content = StringBuilder()
    
    // Add header info
    if (list.isNotEmpty()) {
        content.append("Attendance Report\n")
        content.append("Date: ${list[0].attendance.date}\n")
        content.append("Total: ${list.size}\n")
        content.append("---------------------------\n")
    }

    list.forEach { item ->
        when (option) {
            1 -> content.append("${item.student.rollNumber}\n")
            2 -> content.append("${item.student.name}\n")
            3 -> content.append("${item.student.rollNumber}\t${item.student.name}\n")
        }
    }
    
    try {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { it.write(content.toString().toByteArray()) }
        android.widget.Toast.makeText(context, "Saved to ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error saving file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
