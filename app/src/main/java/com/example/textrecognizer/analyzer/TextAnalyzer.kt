package com.example.textrecognizer.analyzer

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.text.FirebaseVisionText

class TextAnalyzer (private val result: MutableLiveData<String>, val widthCropPercent: Int, val heightCropPercent: Int): ImageAnalysis.Analyzer{
    private val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
    // Flag to skip analyzing new available frames until previous analysis has finished.
    private var isBusy = false
    // rotate camera
    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }

    override fun analyze(imageProxy: ImageProxy, degrees: Int) {
        val mediaImage = imageProxy.image

        val imageRotation = degreesToFirebaseRotation(degrees)
        if (mediaImage != null && !isBusy) {
            isBusy = true
            val bitmap = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation).bitmap
            val croppedWidth = (bitmap.width * (1 - widthCropPercent / 100f)).toInt()
            val croppedHeight = (bitmap.height * (1 - heightCropPercent / 100f)).toInt()
            val x = (bitmap.width - croppedWidth) / 2
            val y = (bitmap.height - croppedHeight) / 2
            val cropBmp = Bitmap.createBitmap(bitmap, x, y, croppedWidth, croppedHeight)
            recognizeTextOnDevice(FirebaseVisionImage.fromBitmap(cropBmp)).addOnCompleteListener {
                isBusy = false
            }
        }
    }

    private fun recognizeTextOnDevice(image: FirebaseVisionImage): Task<FirebaseVisionText> {
        // Pass image to an ML Kit Vision API
        return detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                // Task completed successfully
                result.value = firebaseVisionText.text
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                exception.message.let {
                    Log.e(TAG, it)
                }
            }
    }

    companion object {
        private const val TAG = "TextAnalyzer"
    }
}