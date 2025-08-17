package com.example.ingredients

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ingredients.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    // Pre-Android 10 (API 29) may need WRITE_EXTERNAL_STORAGE to save to Pictures.
    private val requestWritePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // No immediate action; user may press Camera again.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.captureButton.setOnClickListener {
            if (hasCameraPermission()) {
                startActivity(Intent(this, CameraActivity::class.java))
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.quitButton.setOnClickListener {
            // Close the activity + remove from Recents (API 21+).
            // This is the cleanest way to ensure it won’t linger in the running apps list.
            finishAndRemoveTask()
        }

        // Optional: on older devices (API < 29) ask once for WRITE_EXTERNAL_STORAGE
        // so "Save" can write to external Pictures safely.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestWritePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
