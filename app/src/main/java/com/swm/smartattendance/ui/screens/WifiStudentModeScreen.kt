package com.swm.smartattendance.ui.screens

import android.content.Context
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.swm.smartattendance.wifi.WifiClientManager
import java.net.NetworkInterface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiStudentModeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var studentId by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Ready to send attendance") }
    var isSending by remember { mutableStateOf(false) }
    
    val clientManager = remember { WifiClientManager() }
    val deviceMac = remember { getMacAddress() ?: "02:00:00:00:00:00" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Mode (WiFi)", fontWeight = FontWeight.Bold) },
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
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Submit Attendance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Ensure you are connected to the Teacher's Hotspot",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it },
                label = { Text("Enter Your Roll Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (studentId.isBlank()) {
                        Toast.makeText(context, "Please enter your Roll Number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSending = true
                    statusMessage = "Sending..."
                    clientManager.sendAttendance(
                        studentId = studentId,
                        deviceMac = deviceMac,
                        onResult = { success, error ->
                            isSending = false
                            statusMessage = if (success) {
                                "Attendance Submitted Successfully!"
                            } else {
                                "Failed: ${error ?: "Unknown error"}"
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSending && studentId.isNotBlank()
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Attendance")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = statusMessage,
                color = if (statusMessage.contains("Successfully")) Color(0xFF00C853) else if (statusMessage.contains("Failed")) Color(0xFFFF5252) else Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your MAC: $deviceMac",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

fun getMacAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces().toList()
        for (nif in interfaces) {
            if (nif.name.equals("wlan0", ignoreCase = true)) {
                val macBytes = nif.hardwareAddress ?: return null
                val res = StringBuilder()
                for (b in macBytes) {
                    res.append(String.format("%02X:", b))
                }
                if (res.isNotEmpty()) {
                    res.deleteCharAt(res.length - 1)
                }
                return res.toString()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
