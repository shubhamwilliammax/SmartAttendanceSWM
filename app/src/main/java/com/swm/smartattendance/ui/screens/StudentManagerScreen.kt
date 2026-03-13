package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.swm.smartattendance.model.Student
import com.swm.smartattendance.viewmodel.StudentViewModel

/**
 * Student Manager screen - Add, edit, delete students
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagerScreen(
    viewModel: StudentViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            )
        }
    ) { padding ->
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No students. Tap + to add.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    StudentListItem(
                        student = student,
                        onDelete = { viewModel.deleteStudent(student) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        val classes by viewModel.classes.collectAsState()
        AddStudentDialog(
            classes = classes,
            onDismiss = { showAddDialog = false },
            onAdd = { classId, name, rollNo, mac, bleId, faceId ->
                viewModel.addStudent(
                    com.swm.smartattendance.model.Student(
                        classId = classId,
                        name = name,
                        rollNumber = rollNo,
                        macAddress = mac,
                        bleId = bleId,
                        faceId = faceId
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StudentListItem(
    student: Student,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(student.name, style = MaterialTheme.typography.titleMedium)
                Text("Roll: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall)
                student.macAddress?.let { Text("MAC: $it", style = MaterialTheme.typography.bodySmall) }
                student.bleId?.let { Text("BLE: $it", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    classes: List<com.swm.smartattendance.model.AcademicClass>,
    onDismiss: () -> Unit,
    onAdd: (Long, String, String, String?, String?, String?) -> Unit
) {
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: 1L) }
    var name by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }
    var bleId by remember { mutableStateOf("") }
    var faceId by remember { mutableStateOf("") }

    LaunchedEffect(classes) {
        if (selectedClassId == 0L && classes.isNotEmpty()) selectedClassId = classes.first().id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (classes.isNotEmpty()) {
                    Text("Class")
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(classes.find { it.id == selectedClassId }?.name ?: "Select") }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    classes.forEach { cls ->
                        FilterChip(
                            selected = selectedClassId == cls.id,
                            onClick = { selectedClassId = cls.id },
                            label = { Text(cls.name) }
                        )
                    }
                }
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(rollNo, { rollNo = it }, label = { Text("Roll Number") }, singleLine = true)
                OutlinedTextField(mac, { mac = it }, label = { Text("MAC Address (optional)") }, singleLine = true)
                OutlinedTextField(bleId, { bleId = it }, label = { Text("BLE ID (optional)") }, singleLine = true)
                OutlinedTextField(faceId, { faceId = it }, label = { Text("Face ID (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && rollNo.isNotBlank() && selectedClassId > 0L) {
                        onAdd(selectedClassId, name, rollNo, mac.ifBlank { null }, bleId.ifBlank { null }, faceId.ifBlank { null })
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
