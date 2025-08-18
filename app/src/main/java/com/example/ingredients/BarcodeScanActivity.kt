package com.example.ingredients

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.ingredients.databinding.ActivityBarcodeScanBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.OptIn

@OptIn(ExperimentalGetImage::class) // Correct way to use ExperimentalGetImage APIs
class BarcodeScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeScanBinding
    private var analyzer: ImageAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.statusText.text = getString(R.string.ingredients_barcode_ready)

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener {
            // Hook your saving flow here if you want saving from the scanner screen.
            Toast.makeText(this, R.string.ingredients_save, Toast.LENGTH_SHORT).show()
        }

        // Ask for camera permission, then start the camera
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_QR_CODE
                )
                .build()

            val scanner = BarcodeScanning.getClient(options)

            analyzer = ImageAnalysis.Builder()
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val input = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            scanner.process(input)
                                .addOnSuccessListener { barcodes ->
                                    if (barcodes.isNotEmpty()) {
                                        val value = barcodes.first().rawValue.orEmpty()
                                        binding.statusText.text = getString(
                                            R.string.barcode_detected_template,
                                            value
                                        )
                                        // Optionally: return result to caller here and finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Use parameter so it's not "unused"
                                    Log.e("BarcodeScan", "Scanning failed", e)
                                    Toast.makeText(
                                        this,
                                        getString(R.string.ingredients_barcode_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    analyzer
                )
            } catch (e: Exception) {
                Log.e("BarcodeScan", "bindToLifecycle failed", e)
                binding.statusText.text = getString(R.string.ingredients_barcode_error)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    R.string.ingredients_camera_permission_required,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        analyzer = null
    }
}
