package com.manopedia.minimal.translation

import android.util.Log

class TranslationEngine {

    private val labels = arrayOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "del", "nothing", "space"
    ) // Example labels, replace with your actual model labels

    fun interpretResult(output: FloatArray): String {
        // Assuming the output is a probability distribution over the labels
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1

        return if (maxIndex != -1 && maxIndex < labels.size) {
            labels[maxIndex]
        } else {
            "Unknown"
        }
    }
}