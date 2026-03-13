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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as SmartAttendanceApp
        val db = app.database
        setContent {
            SmartAttendanceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        dashboardViewModel = DashboardViewModel.Factory(db.studentDao(), db.attendanceDao()).create(DashboardViewModel::class.java),
                        studentViewModel = StudentViewModel.Factory(db.studentDao(), db.academicClassDao()).create(StudentViewModel::class.java),
                        attendanceViewModel = AttendanceViewModel.Factory(db.attendanceDao(), db.studentDao(), db.subjectDao(), db.academicClassDao()).create(AttendanceViewModel::class.java),
                        routineViewModel = RoutineViewModel.Factory(db.routineSlotDao(), db.academicClassDao(), db.subjectDao()).create(RoutineViewModel::class.java),
                        reportsViewModel = ReportsViewModel.Factory(db.attendanceDao(), db.subjectDao()).create(ReportsViewModel::class.java)
                    )
                }
            }
        }
    }
}
