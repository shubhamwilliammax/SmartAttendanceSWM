package com.swm.smartattendance.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.swm.smartattendance.model.Attendance
import com.swm.smartattendance.model.ClassRoutine
import com.swm.smartattendance.model.Student

/**
 * Room Database for Smart Attendance SWM.
 * Provides offline storage for students, attendance, and routines.
 */
@Database(
    entities = [Student::class, Attendance::class, ClassRoutine::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun classRoutineDao(): ClassRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_attendance_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
