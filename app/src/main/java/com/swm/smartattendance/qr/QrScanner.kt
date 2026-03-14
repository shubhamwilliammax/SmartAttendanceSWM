package com.swm.smartattendance.qr

import android.content.Context
import android.graphics.*
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * QR Code Scanner using ZXing (No native libraries for 16KB page size compatibility).
 * Scans QR codes from camera feed for attendance.
 */
class QrScanner(private val context: Context) {

    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    /**
     * Scan for QR code in ImageProxy from CameraX
     * @return Parsed QR content or null if not found/invalid
     */
    suspend fun scanQrCode(imageProxy: ImageProxy): String? {
        return try {
            val buffer: ByteBuffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            
            val source = PlanarYUVLuminanceSource(
                data,
                imageProxy.width,
                imageProxy.height,
                0, 0,
                imageProxy.width,
                imageProxy.height,
                false
            )
            
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(bitmap)
            result.text
        } catch (e: Exception) {
            null
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        // MultiFormatReader doesn't need explicit closing
    }
}
