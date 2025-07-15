package com.manopedia.minimal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.manopedia.minimal.input.CameraHandler
import com.manopedia.minimal.preprocessing.DataPreprocessor
import com.manopedia.minimal.ui.theme.ManopediaMinimalTheme
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

import com.manopedia.minimal.inference.TFLiteModelHandler
import com.manopedia.minimal.translation.TranslationEngine
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManopediaMinimalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    CameraPreviewScreen(this, context)
                }
            }
        }
    }
}

@Composable
fun CameraPreviewScreen(lifecycleOwner: LifecycleOwner, context: Context) {
    val cameraHandler = remember { CameraHandler(context, lifecycleOwner) }
    val dataPreprocessor = remember { DataPreprocessor(context, "handsigns_quantized_model.tflite") }
    val tfliteModelHandler = remember { TFLiteModelHandler(context, "handsigns_quantized_model.tflite") }
    val translationEngine = remember { TranslationEngine() }
    val previewView = remember { PreviewView(context) }

    var translatedText by remember { mutableStateOf("Initializing...") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                cameraHandler.startCamera(previewView) { imageProxy ->
                    val bitmap = imageProxyToBitmap(imageProxy)
                    bitmap?.let {
                        val preprocessedData = dataPreprocessor.preprocessFrame(it)
                        preprocessedData?.let {
                            val inferenceResult = tfliteModelHandler.runInference(it)
                            inferenceResult?.let {
                                translatedText = translationEngine.interpretResult(it)
                                Log.d("Translation", "Translated: $translatedText")
                            }
                        }
                    }
                    imageProxy.close()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                cameraHandler.stopCamera()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraHandler.stopCamera()
            dataPreprocessor.close()
            tfliteModelHandler.close()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    Text(
        text = translatedText,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val image = imageProxy.image ?: return null

    // Convert YUV_420_888 to NV21
    if (image.format == ImageFormat.YUV_420_888) {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } else {
        Log.e("ImageConverter", "Unsupported image format: ${image.format}")
        return null
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ManopediaMinimalTheme {
        Greeting("Android")
    }
}