package com.swm.smartattendance.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * QR Code generation and validation manager.
 * Generates QR codes for attendance sessions.
 */
object QrCodeManager {

    /**
     * Generate QR code bitmap for attendance session
     * @param content JSON-like string containing session info: date, subject, className, timestamp
     * @param size Width and height of the QR code in pixels
     */
    fun generateQrCode(content: String, size: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 2)
        }

        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun createAttendancePayload(
        date: String,
        subjectId: Long,
        classId: Long,
        sessionId: String
    ): String = "ATTENDANCE|$date|$subjectId|$classId|$sessionId|${System.currentTimeMillis()}"

    fun parseQrContent(content: String): ParsedQrData? {
        return try {
            val parts = content.split("|")
            if (parts.size >= 5 && parts[0] == "ATTENDANCE") {
                ParsedQrData(
                    date = parts[1],
                    subjectId = parts[2].toLongOrNull() ?: 0L,
                    classId = parts[3].toLongOrNull() ?: 0L,
                    sessionId = parts[4],
                    timestamp = parts.getOrNull(5)?.toLongOrNull() ?: 0L
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    data class ParsedQrData(
        val date: String,
        val subjectId: Long,
        val classId: Long,
        val sessionId: String,
        val timestamp: Long
    )
}
