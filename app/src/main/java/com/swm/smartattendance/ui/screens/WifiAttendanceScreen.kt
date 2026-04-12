package com.swm.smartattendance.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import com.swm.smartattendance.viewmodel.WifiAttendanceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WifiAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    wifiAttendanceViewModel: WifiAttendanceViewModel,
    onBack: () -> Unit,
    onFinalize: (String, Long, Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    val permissionState = rememberMultiplePermissionsState(permissions)

    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()
    
    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    
    val isRunning by wifiAttendanceViewModel.isServerRunning.collectAsState()
    val receivedRequests by wifiAttendanceViewModel.receivedRequests.collectAsState()
    val knownStudents by wifiAttendanceViewModel.knownStudents.collectAsState()

    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == 0L) {
            selectedClassId = classes.first().id
            attendanceViewModel.selectClass(classes.first().id)
        }
    }
    LaunchedEffect(selectedClassId) {
        if (selectedClassId > 0L) attendanceViewModel.selectClass(selectedClassId)
    }
    LaunchedEffect(subjects) {
        if (selectedSubjectId == 0L && subjects.isNotEmpty()) {
            selectedSubjectId = subjects.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Attendance (Server)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (selectedSubjectId > 0 && selectedClassId > 0) {
                BottomAppBar(containerColor = Color.Black) {
                    Button(
                        onClick = {
                            wifiAttendanceViewModel.stopServer()
                            onFinalize(DateUtils.getCurrentDate(), selectedSubjectId, selectedClassId)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Finalize")
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Session Configuration", color = Color.White, fontWeight = FontWeight.Bold)
                    
                    if (classes.isNotEmpty()) {
                        Text("Class", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        ScrollableTabRow(
                            selectedTabIndex = classes.indexOfFirst { it.id == selectedClassId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent,
                            divider = {},
                            indicator = {}
                        ) {
                            classes.forEach { cls ->
                                FilterChip(
                                    selected = selectedClassId == cls.id,
                                    onClick = { selectedClassId = cls.id },
                                    label = { Text(cls.name) },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    if (subjects.isNotEmpty()) {
                        Text("Subject", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        ScrollableTabRow(
                            selectedTabIndex = subjects.indexOfFirst { it.id == selectedSubjectId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent,
                            divider = {},
                            indicator = {}
                        ) {
                            subjects.forEach { subj ->
                                FilterChip(
                                    selected = selectedSubjectId == subj.id,
                                    onClick = { selectedSubjectId = subj.id },
                                    label = { Text(subj.name) },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                if (isRunning) wifiAttendanceViewModel.stopServer()
                                else wifiAttendanceViewModel.startServer(DateUtils.getCurrentDate(), selectedSubjectId, selectedClassId)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFFF5252) else Color(0xFF00C853)
                        ),
                        enabled = permissionState.allPermissionsGranted && selectedSubjectId > 0
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRunning) "Stop Server" else "Start Server (Port 8080)")
                    }
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Known",
                    value = knownStudents.size.toString(),
                    color = Color(0xFF00C853),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Unknown",
                    value = (receivedRequests.size - knownStudents.size).toString(),
                    color = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total",
                    value = receivedRequests.size.toString(),
                    color = Color(0xFF2979FF),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Received Requests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(receivedRequests) { request ->
                    val student = knownStudents.find { it.rollNumber == request.studentId }
                    RequestListItem(request, student?.name)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun RequestListItem(request: com.swm.smartattendance.model.WifiAttendanceRequest, studentName: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (studentName != null) Color(0xFF00C853).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Wifi,
                        null,
                        tint = if (studentName != null) Color(0xFF00C853) else Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = studentName ?: "Unknown (ID: ${request.studentId})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MAC: ${request.deviceMac}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
