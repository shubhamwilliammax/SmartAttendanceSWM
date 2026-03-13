package com.swm.smartattendance.parser

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor as ITextPdfExtractor
import java.io.InputStream

/**
 * Extracts text from PDF using iText
 */
object PdfTextExtractor {

    fun extractText(inputStream: InputStream): String {
        val reader = PdfReader(inputStream)
        val pdfDoc = PdfDocument(reader)
        val sb = StringBuilder()
        for (i in 1..pdfDoc.numberOfPages) {
            val page = pdfDoc.getPage(i)
            sb.append(ITextPdfExtractor.getTextFromPage(page))
            sb.append("\n")
        }
        pdfDoc.close()
        reader.close()
        return sb.toString()
    }
}
