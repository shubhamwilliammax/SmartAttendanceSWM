package com.swm.smartattendance.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date formatting.
 * Uses dd/MM/yyyy format for attendance dates.
 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Get current date in dd/MM/yyyy format
     */
    fun getCurrentDate(): String = dateFormat.format(Date())

    /**
     * Get current time in HH:mm format
     */
    fun getCurrentTime(): String = timeFormat.format(Date())

    /**
     * Get current day of week (1=Sunday, 7=Saturday)
     */
    fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Format date to display string
     */
    fun formatDate(date: Date): String = dateFormat.format(date)

    /**
     * Format timestamp to date time string
     */
    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    /**
     * Parse date string to Date object
     */
    fun parseDate(dateString: String): Date? = try {
        dateFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }

    /**
     * Check if given time is within range (HH:mm format)
     */
    fun isTimeInRange(currentTime: String, startTime: String, endTime: String): Boolean {
        return try {
            val current = timeFormat.parse(currentTime) ?: return false
            val start = timeFormat.parse(startTime) ?: return false
            val end = timeFormat.parse(endTime) ?: return false
            !current.before(start) && !current.after(end)
        } catch (e: Exception) {
            false
        }
    }
}
