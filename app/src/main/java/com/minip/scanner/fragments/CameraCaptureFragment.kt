package com.minip.scanner.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.minip.scanner.R
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class CameraCaptureFragment : Fragment(R.layout.camera_capture) {

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    lateinit var previewView : PreviewView
    lateinit var camera_capture_button : Button
    lateinit var capturePreview : ImageView
    lateinit var imageCountView  : TextView

    var imageCount = 0
    var capturedImages : ArrayList<Uri> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity() , REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for take photo button
        previewView = view.findViewById(R.id.viewFinder)
        camera_capture_button = view.findViewById(R.id.camera_capture_button)
        capturePreview = view.findViewById(R.id.capture_preview)
        imageCountView = view.findViewById(R.id.image_count)


        camera_capture_button.setOnClickListener { takePhoto() }
        capturePreview.setOnClickListener { if(imageCount  > 0) { finish() } }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

//    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
//
//        imageCapture.takePicture(
//                 ContextCompat.getMainExecutor(requireActivity()), object: ImageCapture.OnImageCapturedCallback() {
//            override fun onCaptureSuccess (image : ImageProxy) {
//                Toast.makeText(requireActivity(), "Image Captured", Toast.LENGTH_SHORT).show()
//
//
//
//
//            }
//
//            override fun onError(exception: ImageCaptureException) {
//                super.onError(exception)
//                Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        outputDirectory = requireContext().filesDir
        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                UUID.randomUUID().toString() + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                imageCount++
                imageCountView.setText(imageCount.toString());
                capturedImages.add(savedUri)
                capturePreview.setImageURI(savedUri)


            }
        })
    }
    private fun finish() {
        var converterfragment : ConverterFragment = ConverterFragment();

        converterfragment.multiImage(capturedImages)

        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.fragment_container, converterfragment).addToBackStack(null).commit()
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.createSurfaceProvider())
                    }
            imageCapture = ImageCapture.Builder()
                    .build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(activity,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
               requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    companion object {
        private const val TAG = "MiniPScanner"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }


}