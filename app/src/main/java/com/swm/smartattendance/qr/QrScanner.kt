package com.swm.smartattendance.qr

import android.content.Context
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import java.nio.ByteBuffer

/**
 * QR Code Scanner using ML Kit Barcode Scanning.
 * Scans QR codes from camera feed for attendance.
 */
class QrScanner(private val context: Context) {

    private val barcodeScanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    /**
     * Scan for QR code in ImageProxy from CameraX
     * @return Parsed QR content or null if not found/invalid
     */
    suspend fun scanQrCode(imageProxy: ImageProxy): String? {
        return try {
            val inputImage = imageProxyToInputImage(imageProxy) ?: return null
            val barcodes = barcodeScanner.process(inputImage).await()
            barcodes.firstOrNull()?.rawValue
        } catch (e: Exception) {
            null
        }
    }

    private fun imageProxyToInputImage(imageProxy: ImageProxy): InputImage? {
        return try {
            @androidx.camera.core.ExperimentalGetImage
            val mediaImage = imageProxy.image ?: return null
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        barcodeScanner.close()
    }
}
