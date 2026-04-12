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
    const val ATTENDANCE = "attendance"
    const val DOWNLOAD = "download"
    const val SETTINGS = "settings"
    const val SHORT_FORMS = "short_forms"
    
    // Sub-routes
    const val BLE_ATTENDANCE = "ble_attendance"
    const val WIFI_ATTENDANCE = "wifi_attendance"
    const val QR_ATTENDANCE = "qr_attendance"
    const val STUDENT_MANAGER = "student_manager"
    const val ROUTINE_MANAGER = "routine_manager"
    const val REPORTS = "reports"
    const val PREVIEW = "preview/{date}/{subjectId}/{classId}"
    const val WIFI_STUDENT_MODE = "wifi_student_mode"
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
    settingsViewModel: com.swm.smartattendance.viewmodel.SettingsViewModel,
    wifiAttendanceViewModel: com.swm.smartattendance.viewmodel.WifiAttendanceViewModel,
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
                onNavigateToWifiStudent = { navController.navigate(Routes.WIFI_STUDENT_MODE) },
                onMenuClick = onMenuClick
            )
        }
        composable(Routes.UPLOAD) {
            UploadScreen(
                studentViewModel = studentViewModel,
                routineViewModel = routineViewModel,
                onMenuClick = onMenuClick
            )
        }
        composable(Routes.DOWNLOAD) {
            DownloadScreen(
                attendanceViewModel = attendanceViewModel,
                onMenuClick = onMenuClick
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onMenuClick = onMenuClick,
                onNavigateToShortForms = { navController.navigate(Routes.SHORT_FORMS) }
            )
        }
        composable(Routes.SHORT_FORMS) {
            ShortFormManagerScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.BLE_ATTENDANCE) {
            BleAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() },
                onFinalize = { date, subId, classId -> 
                    val encodedDate = java.net.URLEncoder.encode(date, "UTF-8")
                    navController.navigate("preview/$encodedDate/$subId/$classId")
                }
            )
        }
        composable(Routes.WIFI_ATTENDANCE) {
            WifiAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                wifiAttendanceViewModel = wifiAttendanceViewModel,
                onBack = { navController.popBackStack() },
                onFinalize = { date, subId, classId -> 
                    val encodedDate = java.net.URLEncoder.encode(date, "UTF-8")
                    navController.navigate("preview/$encodedDate/$subId/$classId")
                }
            )
        }
        composable(Routes.QR_ATTENDANCE) {
            QrAttendanceScreen(
                studentViewModel = studentViewModel,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() },
                onFinalize = { date, subId, classId -> 
                    val encodedDate = java.net.URLEncoder.encode(date, "UTF-8")
                    navController.navigate("preview/$encodedDate/$subId/$classId")
                }
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
        composable(Routes.WIFI_STUDENT_MODE) {
            WifiStudentModeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PREVIEW) { backStackEntry ->
            val encodedDate = backStackEntry.arguments?.getString("date") ?: ""
            val date = java.net.URLDecoder.decode(encodedDate, "UTF-8")
            val subId = backStackEntry.arguments?.getString("subjectId")?.toLong() ?: 0L
            val classId = backStackEntry.arguments?.getString("classId")?.toLong() ?: 0L
            AttendancePreviewScreen(
                date = date,
                subjectId = subId,
                classId = classId,
                attendanceViewModel = attendanceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
