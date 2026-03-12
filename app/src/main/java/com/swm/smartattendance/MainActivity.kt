package com.swm.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.swm.smartattendance.ui.navigation.NavGraph
import com.swm.smartattendance.ui.theme.SmartAttendanceTheme
import com.swm.smartattendance.viewmodel.AttendanceViewModel
import com.swm.smartattendance.viewmodel.DashboardViewModel
import com.swm.smartattendance.viewmodel.ReportsViewModel
import com.swm.smartattendance.viewmodel.RoutineViewModel
import com.swm.smartattendance.viewmodel.StudentViewModel

/**
 * Main Activity for Smart Attendance SWM.
 * Hosts Compose UI with navigation to all feature screens.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SmartAttendanceApp
        val database = app.database
        setContent {
            SmartAttendanceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        dashboardViewModel = DashboardViewModel.Factory(
                            database.studentDao(),
                            database.attendanceDao()
                        ).create(DashboardViewModel::class.java),
                        studentViewModel = StudentViewModel.Factory(database.studentDao()).create(StudentViewModel::class.java),
                        attendanceViewModel = AttendanceViewModel.Factory(
                            database.attendanceDao(),
                            database.studentDao()
                        ).create(AttendanceViewModel::class.java),
                        routineViewModel = RoutineViewModel.Factory(database.classRoutineDao()).create(RoutineViewModel::class.java),
                        reportsViewModel = ReportsViewModel.Factory(database.attendanceDao()).create(ReportsViewModel::class.java)
                    )
                }
            }
        }
    }
}
