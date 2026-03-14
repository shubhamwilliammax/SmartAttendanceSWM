package com.swm.smartattendance.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.swm.smartattendance.qr.QrCodeManager
import com.swm.smartattendance.qr.QrScanner
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * QR Code Attendance screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit,
    onFinalize: (String, Long, Long) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var rollNumber by remember { mutableStateOf("") }
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
    var isTeacherMode by remember { mutableStateOf(true) }
    var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var lastScannedSessionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (isTeacherMode && selectedSubjectId > 0 && selectedClassId > 0) {
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
        ) {
            // Toggle Teacher/Student mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isTeacherMode,
                    onClick = { isTeacherMode = true },
                    label = { Text("Teacher Mode") }
                )
                FilterChip(
                    selected = !isTeacherMode,
                    onClick = { isTeacherMode = false },
                    label = { Text("Student Mode") }
                )
            }

            if (isTeacherMode) {
                // Teacher: Generate QR
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            classes.forEach { cls ->
                                FilterChip(selected = selectedClassId == cls.id, onClick = { selectedClassId = cls.id }, label = { Text(cls.name) })
                            }
                        }
                        Text("Subject")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            subjects.forEach { subj ->
                                FilterChip(selected = selectedSubjectId == subj.id, onClick = { selectedSubjectId = subj.id }, label = { Text(subj.name) })
                            }
                        }
                        Button(
                            onClick = {
                                if (selectedSubjectId > 0 && selectedClassId > 0) {
                                    val payload = QrCodeManager.createAttendancePayload(
                                        DateUtils.getCurrentDate(),
                                        selectedSubjectId,
                                        selectedClassId,
                                        java.util.UUID.randomUUID().toString()
                                    )
                                    qrBitmap = QrCodeManager.generateQrCode(payload)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate QR Code")
                        }
                    }
                }
                qrBitmap?.let { bitmap ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.size(256.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            } else {
                // Student: Enter roll number, then scan QR
                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Enter Your Roll Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                if (cameraPermission.status.isGranted) {
                    val qrScanner = remember { QrScanner(context) }
                    DisposableEffect(Unit) {
                        onDispose { qrScanner.close() }
                    }

                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                            scope.launch {
                                                val content = qrScanner.scanQrCode(imageProxy)
                                                imageProxy.close()
                                                content?.let { qrContent ->
                                                    val parsed = QrCodeManager.parseQrContent(qrContent)
                                                    parsed?.let { data ->
                                                        if (lastScannedSessionId != data.sessionId && rollNumber.isNotBlank() && data.subjectId > 0 && data.classId > 0) {
                                                            val success = attendanceViewModel.markAttendanceByRollNumber(
                                                                rollNumber,
                                                                data.date,
                                                                data.subjectId,
                                                                data.classId
                                                            )
                                                            withContext(Dispatchers.Main) {
                                                                lastScannedSessionId = data.sessionId
                                                                scanResult = if (success) "Attendance marked!" else "Failed - Student not found or already marked"
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                cameraProvider.unbindAll()
                                try {
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(context))
                            previewView
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    scanResult?.let { result ->
                        Text(
                            result,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                            Text("Grant Camera Permission")
                        }
                    }
                }
            }
        }
    }
}
