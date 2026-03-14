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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.swm.smartattendance.face.FaceRecognitionManager
import com.swm.smartattendance.utils.DateUtils
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel
import kotlinx.coroutines.launch

/**
 * Face Recognition Attendance screen
 * Uses CameraX and ML Kit for face detection
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FaceAttendanceScreen(
    studentViewModel: StudentViewModel,
    attendanceViewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedClassId by remember { mutableStateOf(0L) }
    var selectedSubjectId by remember { mutableStateOf(0L) }
    var lastMarkedFace by remember { mutableStateOf<String?>(null) }

    val classes by attendanceViewModel.classes.collectAsState()
    val subjects by attendanceViewModel.subjects.collectAsState()

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Face Attendance") },
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
            // Session config
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
                    classes.forEach { cls ->
                        FilterChip(
                            selected = selectedClassId == cls.id,
                            onClick = { selectedClassId = cls.id },
                            label = { Text(cls.name) }
                        )
                    }
                    Text("Subject")
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubjectId == subj.id,
                            onClick = { selectedSubjectId = subj.id },
                            label = { Text(subj.name) }
                        )
                    }
                }
            }

            if (cameraPermission.status.isGranted) {
                val faceManager = remember { FaceRecognitionManager(context) }
                val date = remember { DateUtils.getCurrentDate() }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                        
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                        scope.launch {
                                            val faces = faceManager.detectFaces(imageProxy)
                                            if (faces.isNotEmpty() && selectedSubjectId > 0 && selectedClassId > 0) {
                                                val faceId = faceManager.generateFaceId(faces.first())
                                                if (faceId != lastMarkedFace) {
                                                    val success = attendanceViewModel.markAttendanceByFaceId(
                                                        faceId, date, selectedSubjectId, selectedClassId
                                                    )
                                                    if (success) lastMarkedFace = faceId
                                                }
                                            }
                                            imageProxy.close()
                                        }
                                    }
                                }
                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                            cameraProvider.unbindAll()
                            try {
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Text(
                    "Point face at camera to mark attendance",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
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
