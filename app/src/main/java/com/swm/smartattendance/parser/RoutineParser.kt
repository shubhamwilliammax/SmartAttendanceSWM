package com.swm.smartattendance.parser

import com.swm.smartattendance.model.Faculty
import com.swm.smartattendance.model.Subject
import com.swm.smartattendance.utils.ShortFormGenerator
import java.io.InputStream

/**
 * Parses routine from PDF, Excel, or Image.
 * Extracts subjects, faculty, codes, venue, schedule.
 */
class RoutineParser {

    data class ParsedRoutine(
        val className: String,
        val branch: String,
        val semester: Int,
        val session: String,
        val subjects: List<ParsedSubject>,
        val faculty: List<ParsedFaculty>,
        val schedule: List<ParsedSlot>
    )

    data class ParsedSubject(
        val name: String,
        val code: String,
        val shortForm: String,
        val facultyShortName: String? = null,
        val venue: String? = null
    )

    data class ParsedFaculty(
        val name: String,
        val shortName: String
    )

    data class ParsedSlot(
        val dayOfWeek: Int,
        val startTime: String,
        val endTime: String,
        val subjectName: String,
        val facultyShortName: String?,
        val venue: String?
    )

    /**
     * Parse routine from PDF stream
     */
    suspend fun parsePdf(inputStream: InputStream): ParsedRoutine? = try {
        val text = PdfTextExtractor.extractText(inputStream)
        parseFromText(text)
    } catch (e: Exception) {
        null
    }

    /**
     * Parse routine from Excel stream
     */
    suspend fun parseExcel(inputStream: InputStream): ParsedRoutine? = try {
        val text = ExcelTextExtractor.extractText(inputStream)
        parseFromText(text)
    } catch (e: Exception) {
        null
    }

    /**
     * Parse from extracted text (used by PDF, Excel, OCR from image)
     */
    fun parseFromText(text: String): ParsedRoutine? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val className = extractClassName(lines) ?: "B.Tech CSE Semester VI"
        val (branch, semester) = extractBranchAndSemester(className)
        val session = extractSession(text) ?: "${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}-${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 1}"

        val subjectCodeMap = extractSubjectCodes(lines)
        val facultyList = extractFaculty(lines)
        val subjects = subjectCodeMap.map { (code, name) ->
            val shortForm = ShortFormGenerator.generate(name, com.swm.smartattendance.model.ShortFormType.SUBJECT)
            ParsedSubject(name = name, code = code, shortForm = shortForm)
        }.distinctBy { it.code }

        val schedule = extractSchedule(lines, subjectCodeMap)

        return ParsedRoutine(
            className = className,
            branch = branch,
            semester = semester,
            session = session,
            subjects = subjects,
            faculty = facultyList,
            schedule = schedule
        )
    }

    private fun extractClassName(lines: List<String>): String? {
        val semesterRegex = Regex("SEMESTER\\s*[IVX]+|Semester\\s*\\d+", RegexOption.IGNORE_CASE)
        val cseRegex = Regex("CSE|Computer Science", RegexOption.IGNORE_CASE)
        for (line in lines) {
            if (semesterRegex.containsMatchIn(line) && cseRegex.containsMatchIn(line)) {
                return line.trim()
            }
            if (line.contains("B.Tech") && line.contains("SEMESTER", ignoreCase = true)) {
                return line
            }
        }
        return null
    }

    private fun extractBranchAndSemester(className: String): Pair<String, Int> {
        val branch = when {
            className.contains("CSE", true) -> "CSE"
            className.contains("ECE", true) -> "ECE"
            className.contains("EE", true) -> "EE"
            className.contains("ME", true) -> "ME"
            className.contains("CE", true) -> "CE"
            else -> "CSE"
        }
        val semMatch = Regex("(?:SEMESTER|Semester)\\s*([IVX\\d]+)", RegexOption.IGNORE_CASE).find(className)
        val semester = when (semMatch?.groupValues?.get(1)?.uppercase()) {
            "I", "1" -> 1
            "II", "2" -> 2
            "III", "3" -> 3
            "IV", "4" -> 4
            "V", "5" -> 5
            "VI", "6" -> 6
            "VII", "7" -> 7
            "VIII", "8" -> 8
            else -> 6
        }
        return branch to semester
    }

    private fun extractSession(text: String): String? {
        val yearMatch = Regex("20\\d{2}[-–]20\\d{2}").find(text)
        return yearMatch?.value
    }

    private fun extractSubjectCodes(lines: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val codeRegex = Regex("([A-Z]{2,}[A-Z0-9-]*)\\s+([A-Za-z].*)")
        for (line in lines) {
            val match = codeRegex.find(line)
            if (match != null) {
                val code = match.groupValues[1].trim()
                val name = match.groupValues[2].trim()
                if (name.length > 3 && !name.matches(Regex("^[A-Z]{2,3}$"))) {
                    map[code] = name
                }
            }
        }
        return map
    }

    private fun extractFaculty(lines: List<String>): List<ParsedFaculty> {
        val faculty = mutableListOf<ParsedFaculty>()
        val shortNameRegex = Regex("\\(([A-Z]{2,3})\\)")
        for (line in lines) {
            val match = shortNameRegex.find(line)
            if (match != null) {
                val shortName = match.groupValues[1]
                val name = line.substringBefore("(").trim().removeSuffix("Mr.").removeSuffix("Ms.").trim()
                if (name.isNotBlank()) {
                    faculty.add(ParsedFaculty(name = name, shortName = shortName))
                }
            }
        }
        return faculty.distinctBy { it.shortName }
    }

    private fun extractSchedule(lines: List<String>, subjectCodes: Map<String, String>): List<ParsedSlot> {
        val slots = mutableListOf<ParsedSlot>()
        val days = mapOf("MONDAY" to 2, "TUESDAY" to 3, "WEDNESDAY" to 4, "THURSDAY" to 5, "FRIDAY" to 6)
        val timeRegex = Regex("(\\d{1,2}[.:]\\d{2})\\s*[AP]M?\\s*-\\s*(\\d{1,2}[.:]\\d{2})")
        var currentDay = 0
        for (line in lines) {
            val upper = line.uppercase()
            days.forEach { (name, day) ->
                if (upper.contains(name)) currentDay = day
            }
            val timeMatch = timeRegex.find(line)
            if (timeMatch != null && currentDay > 0) {
                val start = timeMatch.groupValues[1].replace(".", ":")
                val end = timeMatch.groupValues[2].replace(".", ":")
                subjectCodes.keys.firstOrNull()?.let { code ->
                    slots.add(ParsedSlot(
                        dayOfWeek = currentDay,
                        startTime = start,
                        endTime = end,
                        subjectName = subjectCodes[code] ?: "",
                        facultyShortName = null,
                        venue = null
                    ))
                }
            }
        }
        return slots
    }
}
