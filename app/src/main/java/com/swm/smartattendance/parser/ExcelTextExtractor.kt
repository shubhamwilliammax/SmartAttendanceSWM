package com.swm.smartattendance.parser

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

/**
 * Extracts text from Excel for parsing
 */
object ExcelTextExtractor {

    fun extractText(inputStream: InputStream): String {
        val workbook = WorkbookFactory.create(inputStream)
        val sb = StringBuilder()
        for (sheet in workbook) {
            for (row in sheet) {
                val rowData = mutableListOf<String>()
                for (cell in row) {
                    rowData.add(when (cell.cellType) {
                        org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
                        org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toLong().toString()
                        else -> ""
                    })
                }
                sb.append(rowData.joinToString("\t"))
                sb.append("\n")
            }
        }
        workbook.close()
        return sb.toString()
    }
}
