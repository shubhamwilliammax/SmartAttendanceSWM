package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.model.ClassRoutine
import com.swm.smartattendance.viewmodel.RoutineViewModel

/**
 * Routine Manager screen
 * Manage class routine for auto subject detection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineManagerScreen(
    viewModel: RoutineViewModel,
    onBack: () -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val dayNames = listOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Routine Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No routines. Upload PDF/image or add manually.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Routine")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(routines, key = { it.id }) { routine ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    dayNames.getOrElse(routine.dayOfWeek) { "Day ${routine.dayOfWeek}" },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "${routine.startTime} - ${routine.endTime}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${routine.subjectName} (${routine.className})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { viewModel.deleteRoutine(routine) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRoutineDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { day, start, end, subject, className ->
                viewModel.addRoutine(day, start, end, subject, className)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, String, String, String, String) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(2) } // Monday default
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var subjectName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }

    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Day of Week")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayNames.forEachIndexed { index, name ->
                        FilterChip(
                            selected = dayOfWeek == index + 1,
                            onClick = { dayOfWeek = index + 1 },
                            label = { Text(name) }
                        )
                    }
                }
                OutlinedTextField(startTime, { startTime = it }, label = { Text("Start (HH:mm)") }, singleLine = true)
                OutlinedTextField(endTime, { endTime = it }, label = { Text("End (HH:mm)") }, singleLine = true)
                OutlinedTextField(subjectName, { subjectName = it }, label = { Text("Subject") }, singleLine = true)
                OutlinedTextField(className, { className = it }, label = { Text("Class") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (startTime.isNotBlank() && endTime.isNotBlank() && subjectName.isNotBlank() && className.isNotBlank()) {
                        onAdd(dayOfWeek, startTime, endTime, subjectName, className)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
