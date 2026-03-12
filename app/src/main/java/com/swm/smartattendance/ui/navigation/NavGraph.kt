package com.swm.smartattendance.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swm.smartattendance.ui.screens.BleAttendanceScreen
import com.swm.smartattendance.ui.screens.DashboardScreen
import com.swm.smartattendance.ui.screens.FaceAttendanceScreen
import com.swm.smartattendance.ui.screens.QrAttendanceScreen
import com.swm.smartattendance.ui.screens.ReportsScreen
import com.swm.smartattendance.ui.screens.RoutineManagerScreen
import com.swm.smartattendance.ui.screens.StudentManagerScreen
import com.swm.smartattendance.ui.screens.WifiAttendanceScreen

/**
 * Navigation routes for the app
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val FACE_ATTENDANCE = "face_attendance"
    const val BLE_ATTENDANCE = "ble_attendance"
    const val WIFI_ATTENDANCE = "wifi_attendance"
    const val QR_ATTENDANCE = "qr_attendance"
    const val STUDENT_MANAGER = "student_manager"
    const val ROUTINE_MANAGER = "routine_manager"
    const val REPORTS = "reports"
}

/**
 * Main navigation graph for Smart Attendance SWM
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    dashboardViewModel: com.swm.smartattendance.viewmodel.DashboardViewModel,
    studentViewModel: com.swm.smartattendance.viewmodel.StudentViewModel,
    attendanceViewModel: com.swm.smartattendance.viewmodel.AttendanceViewModel,
    routineViewModel: com.swm.smartattendance.viewmodel.RoutineViewModel,
    reportsViewModel: com.swm.smartattendance.viewmodel.ReportsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToFace = { navController.navigate(Routes.FACE_ATTENDANCE) },
                onNavigateToBle = { navController.navigate(Routes.BLE_ATTENDANCE) },
                onNavigateToWifi = { navController.navigate(Routes.WIFI_ATTENDANCE) },
                onNavigateToQr = { navController.navigate(Routes.QR_ATTENDANCE) },
                onNavigateToStudents = { navController.navigate(Routes.STUDENT_MANAGER) },
                onNavigateToRoutine = { navController.navigate(Routes.ROUTINE_MANAGER) },
                onNavigateToReports = { navController.navigate(Routes.REPORTS) }
            )
        }
        composable(Routes.FACE_ATTENDANCE) {
            FaceAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BLE_ATTENDANCE) {
            BleAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WIFI_ATTENDANCE) {
            WifiAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.QR_ATTENDANCE) {
            QrAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.STUDENT_MANAGER) {
            StudentManagerScreen(
                viewModel = studentViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ROUTINE_MANAGER) {
            RoutineManagerScreen(
                viewModel = routineViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.REPORTS) {
            ReportsScreen(
                attendanceViewModel = attendanceViewModel,
                reportsViewModel = reportsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
