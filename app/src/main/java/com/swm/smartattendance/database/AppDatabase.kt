package com.swm.smartattendance.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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

private class DatabaseSeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
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
        
        // Seed initial short forms
        db.execSQL("INSERT INTO short_forms (fullName, shortForm, type, isCustom, createdAt) VALUES ('Computer Science Engineering', 'CSE', 'BRANCH', 0, $now)")
        db.execSQL("INSERT INTO short_forms (fullName, shortForm, type, isCustom, createdAt) VALUES ('Electronics and Communication', 'ECE', 'BRANCH', 0, $now)")
        db.execSQL("INSERT INTO short_forms (fullName, shortForm, type, isCustom, createdAt) VALUES ('Mechanical Engineering', 'ME', 'BRANCH', 0, $now)")
        db.execSQL("INSERT INTO short_forms (fullName, shortForm, type, isCustom, createdAt) VALUES ('Civil Engineering', 'CE', 'BRANCH', 0, $now)")
        db.execSQL("INSERT INTO short_forms (fullName, shortForm, type, isCustom, createdAt) VALUES ('Artificial Intelligence', 'AI', 'BRANCH', 0, $now)")
    }
}
