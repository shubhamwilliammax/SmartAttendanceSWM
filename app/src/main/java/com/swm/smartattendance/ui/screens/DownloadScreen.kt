package com.swm.smartattendance.ui.screens

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
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.viewmodel.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    attendanceViewModel: AttendanceViewModel,
    onMenuClick: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(1) } // 1: One Day, 2: Date Range
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var exportFormat by remember { mutableStateOf("Excel") }

    val subjects by attendanceViewModel.subjects.collectAsState()

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
                Text("One Day Attendance")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = selectedOption == 2, onClick = { selectedOption = 2 })
                Text("Date Range")
            }

            if (selectedOption == 1) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Select Date (dd/mm/yyyy)") },
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

            Text("Subject Filter", style = MaterialTheme.typography.titleSmall)
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(subjects.find { it.id == selectedSubjectId }?.name ?: "All Subjects")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("All Subjects") }, onClick = { selectedSubjectId = 0L; expanded = false })
                    subjects.forEach { subj ->
                        DropdownMenuItem(text = { Text(subj.name) }, onClick = { selectedSubjectId = subj.id; expanded = false })
                    }
                }
            }

            Text("Export Format", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Excel", "CSV", "PDF").forEach { format ->
                    FilterChip(
                        selected = exportFormat == format,
                        onClick = { exportFormat = format },
                        label = { Text(format) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* TODO: Trigger Export */ },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Attendance")
            }
        }
    }
}
