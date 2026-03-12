package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import com.swm.smartattendance.wifi.WifiDetectionManager
import kotlinx.coroutines.launch

/**
 * WiFi Hotspot Attendance screen
 * Students connect to teacher's hotspot - MAC address used for attendance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val wifiManager = remember { WifiDetectionManager(context) }

    var subjectName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var macInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()


    val isConnected = wifiManager.isConnectedToWifi()
    val deviceMac = wifiManager.getDeviceMacAddress()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Attendance") },
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Text(
                            if (isConnected) "Connected to WiFi" else "Not connected to WiFi",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    deviceMac?.let { mac ->
                        Text("Your MAC: $mac", style = MaterialTheme.typography.bodySmall)
                    } ?: Text(
                        "MAC address not available (Android 10+ restriction)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Teacher: Enter student MAC to mark attendance",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("Subject") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Class") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = macInput,
                onValueChange = { macInput = it },
                label = { Text("Student MAC Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (subjectName.isBlank() || className.isBlank() || macInput.isBlank()) {
                            statusMessage = "Fill all fields"
                            return@launch
                        }
                        val formatted = wifiManager.formatMacAddress(macInput)
                        val success = attendanceViewModel.markAttendanceByMacAddress(
                            formatted,
                            DateUtils.getCurrentDate(),
                            subjectName,
                            className
                        )
                        statusMessage = if (success) "Attendance marked!" else "Student not found"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark Attendance")
            }

            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(msg, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
