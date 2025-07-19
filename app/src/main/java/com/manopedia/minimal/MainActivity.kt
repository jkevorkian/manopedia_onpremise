package com.manopedia.minimal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isCameraPermissionGranted()) {
            setupContent()
        } else {
            requestCameraPermission()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupContent()
            } else {
                // Handle permission denial gracefully
                Log.e("Permissions", "Camera permission denied")
                // You might want to show a message to the user
            }
        }
    }

    private fun setupContent() {
        setContent {
            ManopediaMinimalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isCameraActive by remember { mutableStateOf(false) }

                    if (isCameraActive) {
                        CameraPreviewScreen(this, LocalContext.current)
                    } else {
                        WelcomeScreen(onStartClick = { isCameraActive = true })
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onStartClick) {
            Text("Start Translating")
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
    var isFrontCamera by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, isFrontCamera) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                cameraHandler.startCamera(previewView, isFrontCamera) { imageProxy ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = translatedText,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(8.dp)
            )
            Button(onClick = { isFrontCamera = !isFrontCamera }) {
                Text(if (isFrontCamera) "Switch to Back Camera" else "Switch to Front Camera")
            }
        }
    }
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