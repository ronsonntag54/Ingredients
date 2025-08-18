package com.example.ingredients

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.ingredients.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { takePhoto() }

        // Ask for camera permission up front
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                provider.unbindAll()
                provider.bindToLifecycle(this, selector, preview, imageCapture)
                cameraProvider = provider
            } catch (t: Throwable) {
                Log.e("CameraActivity", "Camera start failed", t)
                Toast.makeText(
                    this,
                    getString(R.string.ingredients_barcode_error),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: run {
            Toast.makeText(this, R.string.ingredients_capture_not_ready, Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"

        // Save under DCIM/Ingredients
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Ingredients")
            }
        }

        val output = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                    val uri: Uri? = result.savedUri
                    val shownPath = uri?.toString() ?: "DCIM/Ingredients/$fileName"
                    Toast.makeText(
                        this@CameraActivity,
                        getString(R.string.ingredients_saved_to, shownPath),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Capture failed", exception)
                    Toast.makeText(
                        this@CameraActivity,
                        R.string.ingredients_saving_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
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
        try {
            cameraProvider?.unbindAll()
        } catch (_: Throwable) { }
        imageCapture = null
        cameraProvider = null
    }
}
