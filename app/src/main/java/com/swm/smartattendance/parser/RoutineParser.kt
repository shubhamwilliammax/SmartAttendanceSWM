package com.swm.smartattendance.parser

import com.swm.smartattendance.model.ShortFormType
import com.swm.smartattendance.utils.ShortFormGenerator
import java.io.InputStream

/**
 * Robust Routine Parser for PDF and Excel.
 * Extracts: Subjects, Codes, Faculty, and Time Table structure.
 */
class RoutineParser {

    data class ParsedRoutine(
        val className: String,
        val branch: String,
        val semester: Int,
        val session: String,
        val subjects: List<ParsedSubject>,
        val schedule: List<ParsedSlot>
    )

    data class ParsedSubject(
        val name: String,
        val code: String,
        val shortForm: String
    )

    data class ParsedSlot(
        val dayOfWeek: Int, // 2=Mon, 3=Tue, etc (Calendar.MONDAY)
        val startTime: String,
        val endTime: String,
        val subjectName: String
    )

    suspend fun parsePdf(inputStream: InputStream): ParsedRoutine? = try {
        val text = PdfTextExtractor.extractText(inputStream)
        parseFromText(text)
    } catch (e: Exception) {
        null
    }

    suspend fun parseExcel(inputStream: InputStream): ParsedRoutine? = try {
        val text = ExcelTextExtractor.extractText(inputStream)
        parseFromText(text)
    } catch (e: Exception) {
        null
    }

    fun parseFromText(text: String): ParsedRoutine? {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val className = extractClassName(lines) ?: "B.Tech Class"
        val (branch, semester) = extractBranchAndSemester(className)
        val session = extractSession(text) ?: "2024-2025"

        // Step 1: Extract Subject Codes mapping (usually at bottom of table)
        val subjectCodeMap = extractSubjectCodes(lines)
        
        // Step 2: Extract Subjects
        val subjects = subjectCodeMap.map { (code, name) ->
            ParsedSubject(
                name = name,
                code = code,
                shortForm = ShortFormGenerator.generate(name, ShortFormType.SUBJECT)
            )
        }

        // Step 3: Extract Schedule slots
        val schedule = extractSchedule(lines, subjectCodeMap)

        return ParsedRoutine(
            className = className,
            branch = branch,
            semester = semester,
            session = session,
            subjects = subjects,
            schedule = schedule
        )
    }

    private fun extractClassName(lines: List<String>): String? {
        val pattern = Regex("(B\\.Tech|SEMESTER|Semester|CSE|ECE|ME|CE|EE)", RegexOption.IGNORE_CASE)
        return lines.firstOrNull { pattern.containsMatchIn(it) }
    }

    private fun extractBranchAndSemester(className: String): Pair<String, Int> {
        val branch = when {
            className.contains("CSE", true) -> "CSE"
            className.contains("ECE", true) -> "ECE"
            className.contains("ME", true) -> "ME"
            className.contains("CE", true) -> "CE"
            else -> "CSE"
        }
        val semMatch = Regex("(?:SEMESTER|Semester)\\s*([IVX\\d]+)", RegexOption.IGNORE_CASE).find(className)
        val semStr = semMatch?.groupValues?.get(1)?.uppercase() ?: "VI"
        val semester = when (semStr) {
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
        val match = Regex("20\\d{2}[-–]20\\d{2}").find(text)
        return match?.value
    }

    private fun extractSubjectCodes(lines: List<String>): Map<String, String> {
        val codes = mutableMapOf<String, String>()
        // Pattern for "DCSPC 601 Theory of Computation" or "BCSPC 602 Introduction to..."
        val codeRegex = Regex("^([A-Z]{2,}[A-Z0-9-]*)\\s+([A-Za-z].*)")
        
        for (line in lines) {
            val match = codeRegex.find(line)
            if (match != null) {
                val code = match.groupValues[1].trim()
                val name = match.groupValues[2].trim().split(Regex("\\t|\\s{2,}")).first()
                if (name.length > 3) codes[code] = name
            }
        }
        return codes
    }

    private fun extractSchedule(lines: List<String>, subjectCodes: Map<String, String>): List<ParsedSlot> {
        val slots = mutableListOf<ParsedSlot>()
        val dayMap = mapOf("MONDAY" to 2, "TUESDAY" to 3, "WEDNESDAY" to 4, "THURSDAY" to 5, "FRIDAY" to 6)
        
        // Time pattern like "7.30 AM - 8.15 AM"
        val timeRegex = Regex("(\\d{1,2}[.:]\\d{2})\\s*([AP]M)?\\s*-\\s*(\\d{1,2}[.:]\\d{2})\\s*([AP]M)?", RegexOption.IGNORE_CASE)
        
        var currentDay = 0
        for (line in lines) {
            val upper = line.uppercase()
            dayMap.forEach { (name, id) ->
                if (upper.startsWith(name)) currentDay = id
            }
            
            if (currentDay != 0) {
                val matches = timeRegex.findAll(line)
                matches.forEach { match ->
                    val start = match.groupValues[1]
                    val end = match.groupValues[3]
                    
                    // Look for subject code or name near this time in the tab-separated line
                    subjectCodes.forEach { (code, name) ->
                        if (upper.contains(code) || upper.contains(name.uppercase())) {
                            slots.add(ParsedSlot(currentDay, start, end, name))
                        }
                    }
                }
            }
        }
        return slots.distinct()
    }
}
