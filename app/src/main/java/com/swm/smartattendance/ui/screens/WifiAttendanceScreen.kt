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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.swm.smartattendance.model.AttendanceMethod
import kotlinx.coroutines.delay

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
    var includeUnknown by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()
    val students by studentViewModel.students.collectAsState()
    val scanResults by wifiManager.connectedDevices.collectAsState()

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

    // Periodic scanning when active
    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (true) {
                wifiManager.scanConnectedDevices()
                delay(3000) // Scan every 3 seconds
            }
        }
    }

    // Filter results based on known students and toggle
    val filteredResults = remember(scanResults, students, includeUnknown) {
        if (includeUnknown) {
            scanResults
        } else {
            scanResults.filter { device ->
                students.any { it.macAddress?.equals(device.mac, ignoreCase = true) == true }
            }
        }
    }

    val knownCount = scanResults.count { device -> 
        students.any { it.macAddress?.equals(device.mac, ignoreCase = true) == true } 
    }
    val unknownCount = scanResults.size - knownCount

    // Auto-mark attendance logic
    LaunchedEffect(scanResults, selectedSubjectId, selectedClassId) {
        if (isScanning && selectedSubjectId > 0 && selectedClassId > 0) {
            scanResults.forEach { device ->
                val student = students.find { it.macAddress?.equals(device.mac, ignoreCase = true) == true }
                if (student != null) {
                    attendanceViewModel.markAttendanceByMacAddress(
                        device.mac,
                        DateUtils.getCurrentDate(),
                        selectedSubjectId,
                        selectedClassId
                    )
                }
            }
        }
    }

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
                            isScanning = false
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        onClick = { isScanning = !isScanning },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isHotspotActive && permissionState.allPermissionsGranted
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isScanning) "Stop Scanning" else "Start Scanning")
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
                    Text("WiFi & Location permissions required")
                    Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permissions")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredResults, key = { it.mac }) { device ->
                        val student = students.find { it.macAddress?.equals(device.mac, ignoreCase = true) == true }
                        HotspotDeviceListItem(device = device, studentName = student?.name)
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
private fun HotspotDeviceListItem(device: WifiDetectionManager.HotspotDevice, studentName: String?) {
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
                Text(device.mac, style = MaterialTheme.typography.bodySmall)
                Text("IP: ${device.ip}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
