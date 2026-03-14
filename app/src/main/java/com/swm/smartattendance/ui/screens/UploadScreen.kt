package com.swm.smartattendance.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.parser.AttendanceParser
import com.swm.smartattendance.parser.RoutineParser
import com.swm.smartattendance.viewmodel.RoutineViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    studentViewModel: StudentViewModel,
    routineViewModel: RoutineViewModel,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val studentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    if (inputStream != null) {
                        val parser = AttendanceParser()
                        val students = if (it.toString().endsWith(".pdf")) {
                            parser.parsePdf(inputStream)
                        } else {
                            parser.parseExcel(inputStream)
                        }
                        
                        val classId = studentViewModel.getDefaultClassId()
                        students.forEach { ps ->
                            studentViewModel.addStudent(
                                com.swm.smartattendance.model.Student(
                                    classId = classId,
                                    name = ps.name,
                                    rollNumber = ps.rollNumber,
                                    totalClasses = ps.totalClasses,
                                    totalPresent = ps.totalPresent
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Imported ${students.size} students", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val routineLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    if (inputStream != null) {
                        val parser = RoutineParser()
                        val routine = if (it.toString().endsWith(".pdf")) {
                            parser.parsePdf(inputStream)
                        } else {
                            parser.parseExcel(inputStream)
                        }
                        
                        if (routine != null) {
                            routineViewModel.importRoutine(routine)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Routine imported: ${routine.className}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Data") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UploadCard(
                    title = "Import Students",
                    description = "Extract students from Previous Attendance (PDF/Excel)",
                    icon = Icons.Default.People,
                    onClick = { studentLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) }
                )
                
                UploadCard(
                    title = "Import Routine",
                    description = "Import Class Time Table (PDF/Excel)",
                    icon = Icons.Default.Description,
                    onClick = { routineLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) }
                )
            }
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
