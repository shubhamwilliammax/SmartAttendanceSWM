package com.swm.smartattendance

import android.app.Application
import com.swm.smartattendance.database.AppDatabase

/**
 * Application class for Smart Attendance SWM.
 * Initializes database and app-wide dependencies.
 */
class SmartAttendanceApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
