@file:OptIn(ExperimentalMaterial3Api::class)

package com.swm.smartattendance.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.swm.smartattendance.viewmodel.DashboardViewModel

/**
 * Dashboard screen - main entry point with navigation to all features
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToBle: () -> Unit,
    onNavigateToWifi: () -> Unit,
    onNavigateToQr: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToReports: () -> Unit,
    onMenuClick: () -> Unit
) {
    val studentCount by viewModel.studentCount.collectAsState()
    val todayCount by viewModel.todayAttendanceCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Attendance SWM") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Students",
                    value = studentCount.toString(),
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Today's Attendance",
                    value = todayCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Attendance Methods",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Using Box with height because LazyVerticalGrid inside Column with verticalScroll is tricky
            // Better to use a non-lazy grid or fixed layout for dashboard items
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavCard(
                        title = "BLE Proximity",
                        icon = Icons.Default.Bluetooth,
                        onClick = onNavigateToBle,
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        title = "WiFi Hotspot",
                        icon = Icons.Default.Wifi,
                        onClick = onNavigateToWifi,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavCard(
                        title = "QR Code",
                        icon = Icons.Default.QrCode2,
                        onClick = onNavigateToQr,
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        title = "Reports",
                        icon = Icons.Default.Assessment,
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                "Management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavCard(
                        title = "Student Manager",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToStudents,
                        modifier = Modifier.weight(1f)
                    )
                    NavCard(
                        title = "Routine Manager",
                        icon = Icons.Default.Schedule,
                        onClick = onNavigateToRoutine,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NavCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}
