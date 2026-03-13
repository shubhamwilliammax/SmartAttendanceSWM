package com.swm.smartattendance.parser

import java.io.InputStream

/**
 * Parses previous semester attendance from PDF or Excel.
 * Extracts: Student Name, Roll Number, Total Classes, Total Present
 */
class AttendanceParser {

    data class ParsedStudent(
        val name: String,
        val rollNumber: String,
        val totalClasses: Int,
        val totalPresent: Int
    )

    suspend fun parsePdf(inputStream: InputStream): List<ParsedStudent> = try {
        val text = PdfTextExtractor.extractText(inputStream)
        parseFromText(text)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun parseExcel(inputStream: InputStream): List<ParsedStudent> = try {
        val workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(inputStream)
        val students = mutableListOf<ParsedStudent>()
        for (sheet in workbook) {
            var headerRow = -1
            var rollCol = -1
            var nameCol = -1
            var totalCol = -1
            var presentCol = -1
            for ((idx, row) in sheet.withIndex()) {
                if (idx > 50) break
                for ((cIdx, cell) in row.withIndex()) {
                    val cellStr = getCellString(cell).uppercase()
                    when {
                        cellStr.contains("REG") || cellStr.contains("ROLL") -> rollCol = cIdx
                        cellStr.contains("NAME") || cellStr.contains("STUDENT") -> nameCol = cIdx
                        cellStr.contains("TOTAL") && cellStr.contains("CLASS") -> totalCol = cIdx
                        cellStr.contains("PRESENT") || cellStr.contains("ATTENDED") -> presentCol = cIdx
                    }
                }
                if (rollCol >= 0 && nameCol >= 0 && headerRow < 0) headerRow = idx
                if (headerRow >= 0 && idx > headerRow) {
                    val roll = if (rollCol >= 0) getCellString(row.getCell(rollCol)).trim() else ""
                    val name = if (nameCol >= 0) getCellString(row.getCell(nameCol)).trim() else ""
                    if (roll.isNotBlank() && name.isNotBlank()) {
                        val total = if (totalCol >= 0) getCellInt(row.getCell(totalCol)) else 0
                        val present = if (presentCol >= 0) getCellInt(row.getCell(presentCol)) else 0
                        students.add(ParsedStudent(name, roll, total, present))
                    }
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
        val rollRegex = Regex("(SU\\d+[A-Z]*[A-Z0-9-]*)")
        val lines = text.lines()
        for (line in lines) {
            val rollMatch = rollRegex.find(line)
            if (rollMatch != null) {
                val roll = rollMatch.value
                val parts = line.split(Regex("\\s{2,}")).map { it.trim() }.filter { it.isNotBlank() }
                val nameIdx = parts.indexOfFirst { it == roll } + 1
                val name = if (nameIdx > 0 && nameIdx < parts.size) parts[nameIdx] else ""
                val numbers = Regex("\\d+").findAll(line).map { it.value.toIntOrNull() ?: 0 }.toList()
                val total = numbers.getOrNull(numbers.size - 2) ?: 0
                val present = numbers.getOrNull(numbers.size - 1) ?: 0
                if (name.isNotBlank()) {
                    students.add(ParsedStudent(name, roll, total, present))
                }
            }
        }
        return students
    }

    private fun getCellString(cell: org.apache.poi.ss.usermodel.Cell?): String {
        cell ?: return ""
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
            else -> ""
        }
    }

    private fun getCellInt(cell: org.apache.poi.ss.usermodel.Cell?): Int {
        cell ?: return 0
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toInt()
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.toIntOrNull() ?: 0
            else -> 0
        }
    }
}
