package com.swm.smartattendance.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swm.smartattendance.ui.screens.*

/**
 * Navigation routes for the app
 */
object Routes {
    const val DASHBOARD = "dashboard"
    const val UPLOAD = "upload"
    const val ATTENDANCE = "attendance" // This can be a menu or specific screen
    const val DOWNLOAD = "download"
    const val SETTINGS = "settings"
    
    // Sub-routes
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    dashboardViewModel: com.swm.smartattendance.viewmodel.DashboardViewModel,
    studentViewModel: com.swm.smartattendance.viewmodel.StudentViewModel,
    attendanceViewModel: com.swm.smartattendance.viewmodel.AttendanceViewModel,
    routineViewModel: com.swm.smartattendance.viewmodel.RoutineViewModel,
    reportsViewModel: com.swm.smartattendance.viewmodel.ReportsViewModel,
    onMenuClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToBle = { navController.navigate(Routes.BLE_ATTENDANCE) },
                onNavigateToWifi = { navController.navigate(Routes.WIFI_ATTENDANCE) },
                onNavigateToQr = { navController.navigate(Routes.QR_ATTENDANCE) },
                onNavigateToStudents = { navController.navigate(Routes.STUDENT_MANAGER) },
                onNavigateToRoutine = { navController.navigate(Routes.ROUTINE_MANAGER) },
                onNavigateToReports = { navController.navigate(Routes.REPORTS) },
                onMenuClick = onMenuClick
            )
        }
        composable(Routes.UPLOAD) {
            UploadScreen(onMenuClick = onMenuClick)
        }
        composable(Routes.DOWNLOAD) {
            DownloadScreen(
                attendanceViewModel = attendanceViewModel,
                onMenuClick = onMenuClick
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onMenuClick = onMenuClick)
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
                studentViewModel = studentViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.REPORTS) {
            ReportsScreen(
                attendanceViewModel = attendanceViewModel,
                reportsViewModel = reportsViewModel,
                studentViewModel = studentViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
