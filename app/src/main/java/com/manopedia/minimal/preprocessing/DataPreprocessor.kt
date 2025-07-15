package com.manopedia.minimal.preprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.LinkedList

class DataPreprocessor(private val context: Context, private val modelPath: String) {

    private var handLandmarker: HandLandmarker? = null
    // private val windowSize = 30 // Example window size for LSTM input
    // private val featureSize = 21 * 3 // 21 landmarks, each with x, y, z
    // private val slidingWindow = LinkedList<FloatArray>()

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelPath)
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            Log.e("DataPreprocessor", "Failed to create HandLandmarker: ${e.message}")
        }
    }

    fun preprocessFrame(bitmap: Bitmap): ByteBuffer? {
        if (handLandmarker == null) return null

        val mpImage = com.google.mediapipe.framework.image.BitmapImageBuilder(bitmap).build()
        val result = handLandmarker?.detect(mpImage) as HandLandmarkerResult?

        result?.landmarks()?.firstOrNull()?.let { landmarks: List<NormalizedLandmark> ->
            Log.d("DataPreprocessor", "Detected landmarks: ${landmarks.size}")
            // Temporarily return null, will re-add logic later
        }
        return null
    }

    private fun extractAndNormalizeFeatures(landmarks: List<NormalizedLandmark>): FloatArray {
        val features = FloatArray(21 * 3)
        landmarks.forEachIndexed { index, landmark ->
            features[index * 3] = landmark.x()
            features[index * 3 + 1] = landmark.y()
            features[index * 3 + 2] = landmark.z()
        }
        return features
    }

    // private fun formatInputForLSTM(): ByteBuffer {
    //     val byteBuffer = ByteBuffer.allocateDirect(windowSize * featureSize * 4) // 4 bytes per float
    //     byteBuffer.order(ByteOrder.nativeOrder())
    //     val floatBuffer = byteBuffer.asFloatBuffer()
    //
    //     slidingWindow.forEach { frameFeatures ->
    //         floatBuffer.put(frameFeatures)
    //     }
    //
    //     floatBuffer.rewind()
    //     return byteBuffer
    // }

    fun close() {
        handLandmarker?.close()
    }
}