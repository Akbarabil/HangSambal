package com.example.hangsambal.view.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivityCameraBinding
import com.example.hangsambal.util.Camera
import com.example.hangsambal.util.Camera.FILENAME_FORMAT
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.CameraViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding // View binding untuk layout
    private lateinit var viewModel: CameraViewModel     // ViewModel untuk mengatur state kamera
    private lateinit var dialog : ProgressDialog        // Dialog loading
    private var imageCapture: ImageCapture? = null      // Objek untuk menangkap gambar
    private lateinit var outputDirectory: File          // Direktori output untuk menyimpan foto
    private lateinit var cameraExecutor: ExecutorService // Executor untuk proses kamera background

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide() // Sembunyikan action bar

        dialog = ProgressDialog(this)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)

        outputDirectory = getOutputDirectory() // Tentukan direktori penyimpanan
        cameraExecutor = Executors.newSingleThreadExecutor() // Buat thread khusus untuk kamera

        startCamera()

        // Observasi perubahan state kamera dari ViewModel
        viewModel.stateCamera.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                    binding.floatingActionButton.isEnabled = false
                }
                State.LOADING -> {
                    binding.floatingActionButton.isEnabled = false
                    showProgressDialog()
                }
                else -> {
                    dialog.dismiss()
                    binding.floatingActionButton.isEnabled = true
                }
            }
        }

        // Tombol kamera ditekan -> ambil foto
        binding.floatingActionButton.setOnClickListener {
            binding.floatingActionButton.isEnabled = false
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview kamera ditampilkan di viewFinder
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Konfigurasi image capture
            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()

            // Pilih kamera belakang sebagai default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // Lepas semua use case sebelum bind ulang
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch(exc: Exception) {
                Log.e("startCamera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        viewModel.stateCamera.value = State.LOADING

        val imageCapture = imageCapture ?: return

        // Format nama file berdasarkan waktu saat ini
        val name = "Hang_IMG_" + SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Metadata file foto
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HangCameraX-Image")
            }
        }

        // Siapkan opsi output file untuk proses penyimpanan
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Proses pengambilan gambar
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("takePhoto", "Photo capture failed: ${exc.message}", exc)
                    viewModel.stateCamera.value = State.ERROR
                    showAlertDialog(exc.message.toString()) // Tampilkan error ke user
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    // Jika berhasil disimpan, kirim URI kembali ke activity sebelumnya
                    val intent = Intent()
                    intent.putExtra(KeyIntent.KEY_CAMERA, output.savedUri.toString())
                    setResult(Camera.REQUEST_CODE_PERMISSIONS, intent)
                    finish() // Tutup activity
                    viewModel.stateCamera.value = State.COMPLETE
                }
            }
        )
    }

    // Tentukan direktori penyimpanan file hasil kamera
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // Menampilkan dialog alert jika terjadi error saat capture
    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Tampilkan dialog loading saat proses capture berlangsung
    private fun showProgressDialog() {
        dialog.setMessage("Mohon tunggu...")
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown() // Hentikan executor saat activity dihancurkan
    }
}