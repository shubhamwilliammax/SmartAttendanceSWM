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

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SmartAttendanceApp
        val db = app.database
        setContent {
            SmartAttendanceTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("Smart Attendance", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                            Divider()
                            
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
                                label = "Upload",
                                icon = Icons.Default.Upload,
                                selected = currentRoute == Routes.UPLOAD,
                                onClick = {
                                    navController.navigate(Routes.UPLOAD)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            
                            DrawerItem(
                                label = "Attendance",
                                icon = Icons.Default.CheckCircle,
                                selected = false,
                                onClick = {
                                    navController.navigate(Routes.DASHBOARD)
                                    scope.launch { drawerState.close() }
                                }
                            )
                            
                            DrawerItem(
                                label = "Download",
                                icon = Icons.Default.Download,
                                selected = currentRoute == Routes.DOWNLOAD,
                                onClick = {
                                    navController.navigate(Routes.DOWNLOAD)
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
                        }
                    }
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            dashboardViewModel = DashboardViewModel.Factory(db.studentDao(), db.attendanceDao()).create(DashboardViewModel::class.java),
                            studentViewModel = StudentViewModel.Factory(db.studentDao(), db.academicClassDao()).create(StudentViewModel::class.java),
                            attendanceViewModel = AttendanceViewModel.Factory(db.attendanceDao(), db.studentDao(), db.subjectDao(), db.academicClassDao()).create(AttendanceViewModel::class.java),
                            routineViewModel = RoutineViewModel.Factory(db.routineSlotDao(), db.academicClassDao(), db.subjectDao()).create(RoutineViewModel::class.java),
                            reportsViewModel = ReportsViewModel.Factory(db.attendanceDao(), db.subjectDao()).create(ReportsViewModel::class.java),
                            settingsViewModel = SettingsViewModel.Factory(db.shortFormDao(), db.academicClassDao(), db.subjectDao()).create(SettingsViewModel::class.java),
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
