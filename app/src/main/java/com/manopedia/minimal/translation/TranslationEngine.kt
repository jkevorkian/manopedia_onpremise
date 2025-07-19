package com.manopedia.minimal.translation

import android.util.Log
import java.util.LinkedList

class TranslationEngine {

    private val labels = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "del", "nothing", "space"
    ) // Example labels, replace with your actual model labels

    private val predictionBuffer = LinkedList<Int>()
    private val bufferSize = 5 // Number of recent predictions to consider for smoothing

    fun interpretResult(output: FloatArray): String {
        // Get the index of the highest probability
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1

        // Add the current prediction to the buffer
        if (maxIndex != -1) {
            predictionBuffer.add(maxIndex)
            if (predictionBuffer.size > bufferSize) {
                predictionBuffer.removeFirst()
            }
        }

        // Perform smoothing (e.g., majority vote)
        val smoothedIndex = getSmoothedPrediction()

        return if (smoothedIndex != -1 && smoothedIndex < labels.size) {
            labels[smoothedIndex]
        } else {
            "Unknown"
        }
    }

    private fun getSmoothedPrediction(): Int {
        if (predictionBuffer.isEmpty()) return -1

        val counts = mutableMapOf<Int, Int>()
        for (index in predictionBuffer) {
            counts[index] = (counts[index] ?: 0) + 1
        }

        return counts.maxByOrNull { it.value }?.key ?: -1
    }
}