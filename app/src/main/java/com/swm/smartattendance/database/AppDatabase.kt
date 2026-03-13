package com.swm.smartattendance.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.swm.smartattendance.model.*

/**
 * Room Database for Smart Attendance SWM.
 */
@Database(
    entities = [
        AcademicClass::class,
        Subject::class,
        Faculty::class,
        Student::class,
        Attendance::class,
        RoutineSlot::class,
        ShortForm::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun academicClassDao(): AcademicClassDao
    abstract fun subjectDao(): SubjectDao
    abstract fun facultyDao(): FacultyDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun routineSlotDao(): RoutineSlotDao
    abstract fun shortFormDao(): ShortFormDao

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
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private class DatabaseSeedCallback : androidx.room.RoomDatabase.Callback() {
    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        db.execSQL(
            "INSERT INTO academic_classes (name, branch, semester, session, createdAt) " +
            "VALUES ('General Class', 'CSE', 1, '2024-2025', $now)"
        )
        db.execSQL(
            "INSERT INTO subjects (classId, name, code, shortForm, createdAt) " +
            "VALUES (1, 'General', 'GEN', 'GEN', $now)"
        )
    }
}
