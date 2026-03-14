package com.swm.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.swm.smartattendance.ui.navigation.NavGraph
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

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("Smart Attendance", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                            Divider()
                            NavigationDrawerItem(
                                label = { Text("Dashboard") },
                                selected = false,
                                onClick = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
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
