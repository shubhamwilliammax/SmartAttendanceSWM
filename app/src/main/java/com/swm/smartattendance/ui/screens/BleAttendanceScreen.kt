package com.swm.smartattendance.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.swm.smartattendance.bluetooth.BleScanner
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

/**
 * BLE Proximity Attendance screen
 * Scans for nearby BLE devices and marks attendance
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BleAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit,
    onFinalize: (String, Long, Long) -> Unit
) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
    val permissionState = rememberMultiplePermissionsState(permissions)
    val context = LocalContext.current
    val bleScanner = remember { BleScanner(context) }
    val scope = rememberCoroutineScope()

    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var includeUnknown by remember { mutableStateOf(false) }

    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()
    val students by studentViewModel.students.collectAsState()

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
        if (selectedSubjectId == 0L && subjects.isNotEmpty()) selectedSubjectId = subjects.first().id
    }

    val scanResults by bleScanner.scanResults.collectAsState()
    val isScanning by bleScanner.isScanning.collectAsState()

    // Filter results based on known students and toggle
    val filteredResults = remember(scanResults, students, includeUnknown) {
        if (includeUnknown) {
            scanResults
        } else {
            scanResults.filter { device ->
                students.any { it.bleId == device.bleId || it.macAddress == device.address }
            }
        }
    }

    val knownCount = scanResults.count { device -> 
        students.any { it.bleId == device.bleId || it.macAddress == device.address } 
    }
    val unknownCount = scanResults.size - knownCount

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose { bleScanner.stopScan() }
    }

    // Auto-mark and Auto-register attendance logic
    LaunchedEffect(scanResults, selectedSubjectId, selectedClassId) {
        if (isScanning && selectedSubjectId > 0 && selectedClassId > 0) {
            scanResults.forEach { device ->
                var student = students.find { it.bleId == device.bleId || it.macAddress == device.address }
                
                // Auto-registration logic
                if (student == null && device.name.isNotBlank()) {
                    val match = """(\d{3})""".toRegex().find(device.name)
                    if (match != null) {
                        val lastThree = match.value
                        student = students.find { it.rollNumber.endsWith(lastThree) }
                        if (student != null) {
                            // Register student with this BLE/MAC
                            studentViewModel.updateStudent(student.copy(bleId = device.bleId, macAddress = device.address))
                        }
                    }
                }

                if (student != null) {
                    attendanceViewModel.markAttendanceByBleId(
                        device.bleId,
                        DateUtils.getCurrentDate(),
                        selectedSubjectId,
                        selectedClassId
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Attendance") },
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
                            bleScanner.stopScan()
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
                    Text("Session Settings", style = MaterialTheme.typography.titleSmall)
                    
                    if (classes.isNotEmpty()) {
                        Text("Class", style = MaterialTheme.typography.labelSmall)
                        ScrollableTabRow(
                            selectedTabIndex = classes.indexOfFirst { it.id == selectedClassId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            divider = {}
                        ) {
                            classes.forEach { cls ->
                                Tab(
                                    selected = selectedClassId == cls.id,
                                    onClick = { selectedClassId = cls.id },
                                    text = { Text(cls.name) }
                                )
                            }
                        }
                    }

                    if (subjects.isNotEmpty()) {
                        Text("Subject", style = MaterialTheme.typography.labelSmall)
                        ScrollableTabRow(
                            selectedTabIndex = subjects.indexOfFirst { it.id == selectedSubjectId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            divider = {}
                        ) {
                            subjects.forEach { subj ->
                                Tab(
                                    selected = selectedSubjectId == subj.id,
                                    onClick = { selectedSubjectId = subj.id },
                                    text = { Text(subj.name) }
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = includeUnknown, onCheckedChange = { includeUnknown = it })
                        Text("Include Unknown Devices")
                    }

                    Button(
                        onClick = {
                            if (isScanning) bleScanner.stopScan()
                            else bleScanner.startScan()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isScanning) "Stop Attendance" else "Start Attendance")
                    }
                }
            }

            // Live Counters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CounterItem("Known", knownCount.toString(), MaterialTheme.colorScheme.primary)
                    CounterItem("Unknown", unknownCount.toString(), MaterialTheme.colorScheme.secondary)
                    CounterItem("Total", scanResults.size.toString(), MaterialTheme.colorScheme.tertiary)
                }
            }

            if (!permissionState.allPermissionsGranted) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bluetooth & Location permissions required")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredResults, key = { it.address }) { device ->
                        val student = students.find { it.bleId == device.bleId || it.macAddress == device.address }
                        DeviceListItem(device = device, studentName = student?.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DeviceListItem(device: BleScanner.BleDeviceInfo, studentName: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (studentName != null) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(studentName ?: "Unknown Device", style = MaterialTheme.typography.titleSmall)
                Text(device.address, style = MaterialTheme.typography.bodySmall)
                if (device.name.isNotBlank()) {
                    Text("Name: ${device.name}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Text("RSSI: ${device.rssi}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
