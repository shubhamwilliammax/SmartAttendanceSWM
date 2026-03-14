package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit
) {
    var branchShortForm by remember { mutableStateOf("CSE") }
    var subjectShortForm by remember { mutableStateOf("OS") }
    var selectedTheme by remember { mutableStateOf("Light") }
    var accentColor by remember { mutableStateOf("#6200EE") }
    
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Abbreviations Section
            SettingsSection(title = "Abbreviations") {
                OutlinedTextField(
                    value = branchShortForm,
                    onValueChange = { branchShortForm = it },
                    label = { Text("Branch Short Form (e.g. CSE)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subjectShortForm,
                    onValueChange = { subjectShortForm = it },
                    label = { Text("Subject Short Form (e.g. OS)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Theme Section
            SettingsSection(title = "Appearance") {
                Text("Theme", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Light", "Dark", "Monet").forEach { theme ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = { selectedTheme = theme },
                            label = { Text(theme) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = accentColor,
                    onValueChange = { accentColor = it },
                    label = { Text("Custom Accent Color (Hex)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // About Section
            SettingsSection(title = "App Info") {
                OutlinedButton(
                    onClick = { showAbout = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About Smart Attendance SWM")
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Smart Attendance SWM") },
            text = {
                Column {
                    Text("Powered by SWM", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version: 1.0.0")
                    Text("Developer: Senior Android Engineer")
                    Text("Contact: support@swm.com")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Professional attendance management system supporting Bluetooth, WiFi, and QR code methods.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
