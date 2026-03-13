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

/**
 * Utility class for exporting attendance data to PDF and Excel.
 */
object ExportUtils {

    /**
     * Export attendance records to PDF file
     * @param attendanceList List of attendance with student details
     * @param outputFile Destination file
     * @param title Report title
     */
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

            // Header row
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

    /**
     * Export attendance records to Excel file
     * @param attendanceList List of attendance with student details
     * @param outputFile Destination file
     * @param sheetName Name of the Excel sheet
     */
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
                setBorderTop(BorderStyle.THIN)
                setBorderLeft(BorderStyle.THIN)
                setBorderRight(BorderStyle.THIN)
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            }

            var rowNum = 0

            // Header row
            val headerRow = sheet.createRow(rowNum++)
            listOf("Roll Number", "Name", "Date", "Subject", "Class", "Time", "Method").forEachIndexed { index, header ->
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
                row.createCell(6).setCellValue(item.attendance.method.name)
            }

            // Auto-size columns
            for (i in 0..6) {
                sheet.autoSizeColumn(i)
            }

            FileOutputStream(outputFile).use { workbook.write(it) }
            workbook.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
