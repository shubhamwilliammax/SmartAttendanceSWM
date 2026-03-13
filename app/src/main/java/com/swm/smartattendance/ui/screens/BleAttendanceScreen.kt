package com.swm.smartattendance.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
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
    onBack: () -> Unit
) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
        )
    }
    val permissionState = rememberMultiplePermissionsState(permissions)
    val context = LocalContext.current
    val bleScanner = remember { BleScanner(context) }
    val scope = rememberCoroutineScope()

    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
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
        if (selectedSubjectId == 0L && subjects.isNotEmpty()) selectedSubjectId = subjects.first().id
    }

    val scanResults by bleScanner.scanResults.collectAsState()
    val isScanning by bleScanner.isScanning.collectAsState()

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose { bleScanner.stopScan() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE Attendance") },
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
                    Text("Class")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        classes.forEach { cls ->
                            FilterChip(selected = selectedClassId == cls.id, onClick = { selectedClassId = cls.id }, label = { Text(cls.name) })
                        }
                    }
                    Text("Subject")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        subjects.forEach { subj ->
                            FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id }, label = { Text(subj.name) })
                        }
                    }
                    Button(
                        onClick = {
                            if (isScanning) bleScanner.stopScan()
                            else bleScanner.startScan()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isScanning) "Stop Scan" else "Start BLE Scan")
                    }
                }
            }

            if (!permissionState.allPermissionsGranted) {
                Text(
                    "Bluetooth permissions required",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Text(
                    "Scanned devices (tap to mark attendance):",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scanResults, key = { it.address }) { device ->
                        Card(
                            onClick = {
                                if (selectedSubjectId > 0 && selectedClassId > 0) {
                                    scope.launch {
                                        attendanceViewModel.markAttendanceByBleId(
                                            device.bleId,
                                            DateUtils.getCurrentDate(),
                                            selectedSubjectId,
                                            selectedClassId
                                        )
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(device.name, style = MaterialTheme.typography.titleSmall)
                                    Text(device.bleId, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("RSSI: ${device.rssi}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
