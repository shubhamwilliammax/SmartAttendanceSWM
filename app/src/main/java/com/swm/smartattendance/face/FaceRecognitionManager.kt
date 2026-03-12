package com.swm.smartattendance.face

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

/**
 * Face Recognition Manager using ML Kit Face Detection.
 * Detects faces from camera and matches against registered face IDs.
 */
class FaceRecognitionManager(private val context: Context) {

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    /**
     * Process image and detect faces
     * @return List of detected faces with bounding boxes
     */
    suspend fun detectFaces(imageProxy: ImageProxy): List<Face> {
        return try {
            val inputImage = imageProxyToInputImage(imageProxy) ?: return emptyList()
            val result = faceDetector.process(inputImage).await()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            emptyList()
        }
    }

    /**
     * Process bitmap and detect faces
     */
    suspend fun detectFacesFromBitmap(bitmap: Bitmap): List<Face> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = faceDetector.process(inputImage).await()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Face detection from bitmap failed", e)
            emptyList()
        }
    }

    /**
     * Generate face ID from face detection result.
     * In production, this would use face embedding for recognition.
     * For demo: uses tracking ID or face bounds hash as identifier.
     */
    fun generateFaceId(face: Face): String {
        val trackingId = face.trackingId
        return if (trackingId != null && trackingId > 0) {
            "face_$trackingId"
        } else {
            val bounds = face.boundingBox
            "face_${bounds.hashCode()}_${System.currentTimeMillis()}"
        }
    }

    /**
     * Extract face region as bitmap for storage/registration
     */
    fun extractFaceRegion(bitmap: Bitmap, face: Face): Bitmap {
        val bounds = face.boundingBox
        val padding = 20
        val left = (bounds.left - padding).coerceAtLeast(0)
        val top = (bounds.top - padding).coerceAtLeast(0)
        val right = (bounds.right + padding).coerceAtMost(bitmap.width)
        val bottom = (bounds.bottom + padding).coerceAtMost(bitmap.height)
        val width = right - left
        val height = bottom - top
        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    @ExperimentalGetImage
    private fun imageProxyToInputImage(imageProxy: ImageProxy): InputImage? {
        return try {
            val mediaImage = imageProxy.image ?: return null
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert ImageProxy", e)
            null
        }
    }

    /**
     * Close detector to release resources
     */
    fun close() {
        faceDetector.close()
    }

    companion object {
        private const val TAG = "FaceRecognitionManager"
    }
}
