package com.manopedia.minimal.inference

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModelHandler(private val context: Context, private val modelPath: String) {

    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, modelPath))
        } catch (e: Exception) {
            Log.e("TFLiteModelHandler", "Failed to create TFLite interpreter: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun runInference(inputBuffer: ByteBuffer): FloatArray? {
        if (interpreter == null) return null

        val outputBuffer = ByteBuffer.allocateDirect(interpreter!!.getOutputTensor(0).numBytes())
        interpreter?.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val result = FloatArray(outputBuffer.remaining() / 4) // 4 bytes per float
        outputBuffer.asFloatBuffer().get(result)
        return result
    }

    fun close() {
        interpreter?.close()
    }
}