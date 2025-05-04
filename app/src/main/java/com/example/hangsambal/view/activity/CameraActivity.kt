package com.example.hangsambal.view.activity

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.hangsambal.databinding.ActivityCameraBinding
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.CameraViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//class CameraActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityCameraBinding
//    private lateinit var viewModel: CameraViewModel
//    private lateinit var dialog : ProgressDialog
//    private var imageCapture: ImageCapture? = null
//    private lateinit var outputDirectory: File
//    private lateinit var cameraExecutor: ExecutorService
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityCameraBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        supportActionBar?.hide()
//
//        dialog = ProgressDialog(this)
//        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
//
//        outputDirectory = getOutputDirectory()
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        startCamera()
//
//        viewModel.stateCamera.observe(this) {
//            when (it) {
//                State.COMPLETE -> {
//                    dialog.dismiss()
//                    binding.floatingActionButton.isEnabled = false
//                }
//                State.LOADING -> {
//                    binding.floatingActionButton.isEnabled = false
//                    showProgressDialog()
//                }
//                else -> {
//                    dialog.dismiss()
//                    binding.floatingActionButton.isEnabled = true
//                }
//            }
//        }
//
//        binding.floatingActionButton.setOnClickListener {
//            binding.floatingActionButton.isEnabled = false
//            takePhoto()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//                }
//
//            imageCapture = ImageCapture.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                .build()
//
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, imageCapture)
//
//            } catch(exc: Exception) {
//                Log.e("startCamera", "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun takePhoto() {
//        viewModel.stateCamera.value = State.LOADING
//        val imageCapture = imageCapture ?: return
//        val name = "Finna_IMG_" + SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FinnaCameraX-Image")
//            }
//        }
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues)
//            .build()
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e("takePhoto", "Photo capture failed: ${exc.message}", exc)
//                    viewModel.stateCamera.value = State.ERROR
//                    showAlertDialog(exc.message.toString())
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults){
//                    val intent = Intent()
//                    intent.putExtra(KeyIntent.KEY_CAMERA, output.savedUri.toString())
//                    setResult(Camera.REQUEST_CODE_PERMISSIONS, intent)
//                    finish()
//                    viewModel.stateCamera.value = State.COMPLETE
//                }
//            }
//        )
//    }
//
//    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, resources.getString(R.string.app_name)).apply {
//                mkdirs()
//            }
//        }
//        return if (mediaDir != null && mediaDir.exists())
//            mediaDir else filesDir
//    }
//
//    private fun showAlertDialog(message: String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Pesan")
//        builder.setMessage(message)
//        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
//            dialog.dismiss()
//        }
//        builder.show()
//    }
//
//    private fun showProgressDialog() {
//        //show dialog
//        dialog.setMessage("Mohon tunggu...")
//        dialog.setCancelable(false)
//        dialog.show()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//    }
//}