package com.swm.smartattendance.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import com.swm.smartattendance.wifi.WifiDetectionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WifiAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit,
    onFinalize: (String, Long, Long) -> Unit
) {
    val context = LocalContext.current
    val wifiManager = remember { WifiDetectionManager(context) }
    val scope = rememberCoroutineScope()

    // Permissions required for WiFi/Hotspot functionality
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    val permissionState = rememberMultiplePermissionsState(permissions)

    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var macInput by remember { mutableStateOf("") }
    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()

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

    var statusMessage by remember { mutableStateOf<String?>(null) }
    // Hotspot state check
    val isHotspotActive = wifiManager.isHotspotEnabled()
    val deviceMac = try { wifiManager.getDeviceMacAddress() } catch (e: Exception) { null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hotspot Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (selectedSubjectId > 0 && selectedClassId > 0) {
                BottomAppBar {
                    Button(
                        onClick = {
                            onFinalize(DateUtils.getCurrentDate(), selectedSubjectId, selectedClassId)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Attendance")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!permissionState.allPermissionsGranted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Permissions are required to use Hotspot Attendance", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permissions")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHotspotActive) MaterialTheme.colorScheme.primaryContainer 
                                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Text(
                            if (isHotspotActive) "Hotspot is Active" else "Please enable hotspot to start attendance.",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (isHotspotActive) {
                        deviceMac?.let { mac ->
                            Text("Your Device MAC: $mac", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Class", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                classes.forEach { cls ->
                    FilterChip(
                        selected = selectedClassId == cls.id,
                        onClick = { selectedClassId = cls.id },
                        label = { Text(cls.name) }
                    )
                }
            }
            
            Text("Subject", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.forEach { subj ->
                    FilterChip(
                        selected = selectedSubjectId == subj.id,
                        onClick = { selectedSubjectId = subj.id },
                        label = { Text(subj.name) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = macInput,
                onValueChange = { macInput = it },
                label = { Text("Student MAC Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        if (selectedSubjectId == 0L || macInput.isBlank()) {
                            Toast.makeText(context, "Select subject and enter MAC", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val formattedMac = wifiManager.formatMacAddress(macInput)
                        val success = attendanceViewModel.markAttendanceByMacAddress(
                            formattedMac,
                            DateUtils.getCurrentDate(),
                            selectedSubjectId,
                            selectedClassId
                        )
                        if (success) {
                            statusMessage = "Attendance marked for student!"
                            macInput = ""
                        } else {
                            statusMessage = "Student not found or attendance already marked."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isHotspotActive && permissionState.allPermissionsGranted
            ) {
                Text("Mark Attendance")
            }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
