package com.swm.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swm.smartattendance.ui.navigation.NavGraph
import com.swm.smartattendance.ui.navigation.Routes
import com.swm.smartattendance.ui.theme.SmartAttendanceTheme
import com.swm.smartattendance.viewmodel.*
import kotlinx.coroutines.launch

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SmartAttendanceApp
        val db = app.database
        setContent {
            SmartAttendanceTheme(darkTheme = true) { // Force dark theme for premium look
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "Smart Attendance",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))

                            DrawerItem(
                                label = "Dashboard",
                                icon = Icons.Default.Dashboard,
                                selected = currentRoute == Routes.DASHBOARD,
                                onClick = {
                                    navController.navigate(Routes.DASHBOARD) {
                                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                                    }
                                    scope.launch { drawerState.close() }
                                }
                            )
                            
                            DrawerItem(
                                label = "Take Attendance",
                                icon = Icons.Default.CheckCircle,
                                selected = currentRoute == Routes.BLE_ATTENDANCE || currentRoute == Routes.WIFI_ATTENDANCE || currentRoute == Routes.QR_ATTENDANCE,
                                onClick = {
                                    navController.navigate(Routes.DASHBOARD)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Upload Center",
                                icon = Icons.Default.CloudUpload,
                                selected = currentRoute == Routes.UPLOAD,
                                onClick = {
                                    navController.navigate(Routes.UPLOAD)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Students",
                                icon = Icons.Default.People,
                                selected = currentRoute == Routes.STUDENT_MANAGER,
                                onClick = {
                                    navController.navigate(Routes.STUDENT_MANAGER)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Routine",
                                icon = Icons.Default.CalendarToday,
                                selected = currentRoute == Routes.ROUTINE_MANAGER,
                                onClick = {
                                    navController.navigate(Routes.ROUTINE_MANAGER)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Reports",
                                icon = Icons.Default.Assessment,
                                selected = currentRoute == Routes.REPORTS,
                                onClick = {
                                    navController.navigate(Routes.REPORTS)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Settings",
                                icon = Icons.Default.Settings,
                                selected = currentRoute == Routes.SETTINGS,
                                onClick = {
                                    navController.navigate(Routes.SETTINGS)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Recycle Bin",
                                icon = Icons.Default.Delete,
                                selected = false,
                                onClick = { scope.launch { drawerState.close() } }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            DrawerItem(
                                label = "Sign Out",
                                icon = Icons.AutoMirrored.Filled.Logout,
                                selected = false,
                                onClick = { finish() }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF000000) // Deep black background
                    ) {
                        val attendanceViewModel = AttendanceViewModel.Factory(db.attendanceDao(), db.studentDao(), db.subjectDao(), db.academicClassDao()).create(AttendanceViewModel::class.java)
                        NavGraph(
                            navController = navController,
                            dashboardViewModel = DashboardViewModel.Factory(db.studentDao(), db.attendanceDao()).create(DashboardViewModel::class.java),
                            studentViewModel = StudentViewModel.Factory(db.studentDao(), db.academicClassDao()).create(StudentViewModel::class.java),
                            attendanceViewModel = attendanceViewModel,
                            routineViewModel = RoutineViewModel.Factory(db.routineSlotDao(), db.academicClassDao(), db.subjectDao()).create(RoutineViewModel::class.java),
                            reportsViewModel = ReportsViewModel.Factory(db.attendanceDao(), db.subjectDao()).create(ReportsViewModel::class.java),
                            settingsViewModel = SettingsViewModel.Factory(db.shortFormDao(), db.academicClassDao(), db.subjectDao()).create(SettingsViewModel::class.java),
                            wifiAttendanceViewModel = WifiAttendanceViewModel.Factory(db.studentDao(), db.attendanceDao(), attendanceViewModel).create(WifiAttendanceViewModel::class.java),
                            onMenuClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
