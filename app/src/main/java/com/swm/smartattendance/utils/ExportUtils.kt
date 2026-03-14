package com.swm.smartattendance.utils

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.swm.smartattendance.model.AttendanceWithStudent
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

/**
 * Utility class for exporting attendance data to various formats.
 */
object ExportUtils {

    fun exportToPdf(
        attendanceList: List<AttendanceWithStudent>,
        outputFile: File,
        title: String = "Attendance Report"
    ): Result<File> {
        return try {
            val writer = PdfWriter(outputFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            document.add(Paragraph(title).setFontSize(18f).setBold())
            document.add(Paragraph("Generated on: ${DateUtils.formatDateTime(System.currentTimeMillis())}").setFontSize(10f))
            document.add(Paragraph(" "))

            val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 3f, 2f, 3f, 3f)))
                .useAllAvailableWidth()
                .setFontSize(10f)

            table.addHeaderCell(Cell().add(Paragraph("Roll No").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            table.addHeaderCell(Cell().add(Paragraph("Name").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            table.addHeaderCell(Cell().add(Paragraph("Date").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            table.addHeaderCell(Cell().add(Paragraph("Subject").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))
            table.addHeaderCell(Cell().add(Paragraph("Time").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY))

            attendanceList.forEach { item ->
                table.addCell(item.student.rollNumber)
                table.addCell(item.student.name)
                table.addCell(item.attendance.date)
                table.addCell(item.subject.name)
                table.addCell(DateUtils.formatDateTime(item.attendance.markedAt))
            }

            document.add(table)
            document.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportToExcel(
        attendanceList: List<AttendanceWithStudent>,
        outputFile: File,
        sheetName: String = "Attendance"
    ): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(sheetName)
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                setBorderBottom(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
            }

            var rowNum = 0
            val headerRow = sheet.createRow(rowNum++)
            listOf("Roll Number", "Name", "Date", "Subject", "Class", "Time").forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            attendanceList.forEach { item ->
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(item.student.rollNumber)
                row.createCell(1).setCellValue(item.student.name)
                row.createCell(2).setCellValue(item.attendance.date)
                row.createCell(3).setCellValue(item.subject.name)
                row.createCell(4).setCellValue(item.academicClass.name)
                row.createCell(5).setCellValue(DateUtils.formatDateTime(item.attendance.markedAt))
            }

            for (i in 0..5) sheet.autoSizeColumn(i)
            FileOutputStream(outputFile).use { workbook.write(it) }
            workbook.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportToCsv(attendanceList: List<AttendanceWithStudent>, outputFile: File): Result<File> {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.append("Roll Number,Name,Date,Subject,Class,Time\n")
                attendanceList.forEach { item ->
                    writer.append("${item.student.rollNumber},${item.student.name},${item.attendance.date},${item.subject.name},${item.academicClass.name},${DateUtils.formatDateTime(item.attendance.markedAt)}\n")
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportToText(attendanceList: List<AttendanceWithStudent>, outputFile: File): Result<File> {
        return try {
            FileWriter(outputFile).use { writer ->
                attendanceList.forEach { item ->
                    writer.append("${item.student.rollNumber} - ${item.student.name} - Present\n")
                }
            }
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
