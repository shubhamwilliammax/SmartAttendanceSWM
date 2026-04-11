package com.swm.smartattendance.parser

import java.io.InputStream
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Robust Attendance Parser.
 * Specifically optimized for university-style PDF/Excel student lists.
 */
class AttendanceParser {

    data class ParsedStudent(
        val name: String,
        val rollNumber: String,
        val totalClasses: Int = 0,
        val totalPresent: Int = 0
    )

    suspend fun parsePdf(inputStream: InputStream): List<ParsedStudent> {
        val text = PdfTextExtractor.extractText(inputStream)
        return parseFromText(text)
    }

    suspend fun parseExcel(inputStream: InputStream): List<ParsedStudent> = try {
        val workbook = WorkbookFactory.create(inputStream)
        val students = mutableListOf<ParsedStudent>()
        val sheet = workbook.getSheetAt(0)
        
        var rollCol = -1
        var nameCol = -1
        
        // Dynamic Column Detection
        for (row in sheet) {
            for (cell in row) {
                val valStr = cell.toString().uppercase()
                if (valStr.contains("ROLL") || valStr.contains("REG")) rollCol = cell.columnIndex
                if (valStr.contains("NAME") || valStr.contains("STUDENT")) nameCol = cell.columnIndex
            }
            if (rollCol != -1 && nameCol != -1) break
        }
        
        if (rollCol != -1 && nameCol != -1) {
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val roll = row.getCell(rollCol)?.toString()?.trim() ?: ""
                val name = row.getCell(nameCol)?.toString()?.trim() ?: ""
                if (roll.startsWith("SU") || roll.length > 5) {
                    students.add(ParsedStudent(name, roll))
                }
            }
        }
        workbook.close()
        students
    } catch (e: Exception) {
        emptyList()
    }

    fun parseFromText(text: String): List<ParsedStudent> {
        val students = mutableListOf<ParsedStudent>()
        // Regex matches "SU" followed by digits and uppercase letters (e.g., SU23BTECHCSE001)
        val rollRegex = Regex("(SU\\d+[A-Z0-9-]*)", RegexOption.IGNORE_CASE)
        
        text.lines().forEach { line ->
            val match = rollRegex.find(line)
            if (match != null) {
                val roll = match.value
                val parts = line.split(roll)
                
                // Name is usually the longest alphabetical part remaining in the line
                val potentialName = if (parts.size > 1) parts[1] else parts[0]
                val cleanedName = potentialName.trim()
                    .split(Regex("\\s{2,}|\\t")) // Split by large gaps
                    .firstOrNull { it.any { c -> c.isLetter() } } ?: ""
                
                val finalName = cleanedName.replace(Regex("[^a-zA-Z\\s]"), "").trim()
                
                if (finalName.length > 2 && finalName.split(" ").size >= 1) {
                    students.add(ParsedStudent(finalName, roll))
                }
            }
        }
        return students.distinctBy { it.rollNumber }
    }
}
