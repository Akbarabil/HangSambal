package com.example.hangsambal.view.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.hangsambal.R
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityDocumentationBinding
import com.example.hangsambal.util.Camera
import com.example.hangsambal.util.JWTUtils
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.DocumentationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentationActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityDocumentationBinding
    private lateinit var viewModel: DocumentationViewModel
    private lateinit var dialog: ProgressDialog
    private lateinit var dialogLocation: ProgressDialog
    private lateinit var locationManager: LocationManager
    private lateinit var transaction: com.example.hangsambal.model.request.PostTransaction
    private var currentPhotoPath: String = ""
    private var address: String = ""
    private var isFirstOpenCamera: Boolean = true
    private var isFotoDisplay: Boolean = false
    private var photoDisplay: File? = null
    private var photoLapak: File? = null
    private var photoDisplayPath: String? = null
    private var photoLapakPath: String? = null
    private var latitude: String? = null
    private var longitude: String? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var locationCallback: LocationCallback

    val locationPermissionCode = 2
    val REQUEST_CODE = 200

    private var isFakeGPS: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialogLocation = ProgressDialog(this)

        transaction = intent.getParcelableExtra(KeyIntent.KEY_TRANSACTION)!!

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000
        locationSettingsRequestBuilder = LocationSettingsRequest.Builder()
        locationSettingsRequestBuilder.addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.lastLocation != null) {
                    getGeocoder(p0.lastLocation!!)
                } else {
                    Toast.makeText(
                        this@DocumentationActivity,
                        "Tidak dapat mendapatkan lokasi, silahkan coba lagi",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
        viewModel = ViewModelProvider(this).get(DocumentationViewModel::class.java)
        checkSelfPermission()

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

        binding.cardViewFotoDisplay.setOnClickListener {
            isFotoDisplay = true
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (address.isEmpty()) {
                    startLocationUpdates()
                } else {
                    openCamera()
                }
            } else {
                Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardViewFotoLapak.setOnClickListener {
            isFotoDisplay = false
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (address.isEmpty()) {
                    startLocationUpdates()
                } else {
                    openCamera()
                }
            } else {
                Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.stateCamera.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }

                State.LOADING -> {
                    showProgressDialog("Mohon tunggu...")
                }

                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.stateLocation.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialogLocation.dismiss()
                }

                State.LOADING -> {
                    showProgressDialogLocation()
                }

                else -> {
                    dialogLocation.dismiss()
                    showAlertDialog(
                        "Terjadi kesalahan saat mendapatkan lokasi, mohon coba lagi",
                        false
                    )
                }
            }
        }

        viewModel.stateTransaction.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    binding.materialButtonKirim.isEnabled = true
                    dialog.dismiss()
                }

                State.LOADING -> {
                    showProgressDialog("Mengirim data...")
                    binding.materialButtonKirim.isEnabled = false
                }

                else -> {
                    dialog.dismiss()
                    binding.materialButtonKirim.isEnabled = true
                }
            }
        }

        viewModel.stateImageTransaction.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    binding.materialButtonKirim.isEnabled = true
                    dialog.dismiss()
                    startActivity(Intent(this, FeedbackActivity::class.java))
                }

                State.LOADING -> {
                    showProgressDialog("Mengirim gambar...")
                    binding.materialButtonKirim.isEnabled = false
                }

                else -> {
                    dialog.dismiss()
                    binding.materialButtonKirim.isEnabled = true
                }
            }
        }

        binding.materialButtonKirim.setOnClickListener {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location == null) {
                    showAlertDialog("Gagal mendapatkan lokasi", false)
                    return@addOnSuccessListener
                }

                if (photoDisplay == null || photoLapak == null) {
                    showAlertDialog("Mohon upload foto display dan foto lapak", false)
                } else {
                    InternetUtils.checkInternetBeforeAction(this) {
                        binding.materialButtonKirim.isEnabled = false

                        transaction.latTrans = location.latitude.toString()
                        transaction.longTrans = location.longitude.toString()
                        transaction.isFakeGPS = if (isFakeGPS) 1 else 0

                        viewModel.postTransaction(
                            this,
                            transaction,
                            photoDisplay!!,
                            photoLapak!!
                        )
                    }
                }
            }.addOnFailureListener {
                showAlertDialog("Gagal mendapatkan lokasi", false)
            }
        }


        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), false)
            }
        }

        viewModel.errorMessageImage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), true)
            }
        }

    }

    fun isMockLocation(location: Location): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock()  // Buat API 31+
        } else {
            location.isFromMockProvider  // Buat API 18-30
        }
    }

    private fun checkSelfPermission() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                locationPermissionCode
            )
        }
    }

    private fun showProgressDialog(message: String) {
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showProgressDialogLocation() {
        dialogLocation.setMessage("Sedang mencari lokasi...")
        dialogLocation.setCancelable(false)
        dialogLocation.show()
    }

    private fun showAlertDialog(message: String, isErrorImage: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        if (isErrorImage) {
            builder.setCancelable(false)
            builder.setPositiveButton("Kirim") { dialog, which ->
                viewModel.idTrans.observe(this) {
                    viewModel.postImageTransaction(
                        this,
                        it.toString(),
                        photoDisplay!!,
                        photoLapak!!
                    )
                    dialog.dismiss()
                }
            }
        } else {
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun showAlertDialogFakeGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setCancelable(false)
        builder.setMessage("Anda terdeteksi menggunakan lokasi palsu")
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()

            Prefs(this).jwt = null
            Prefs(this).idDistrict = null
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        builder.show()
    }

    private fun openCamera() {
        viewModel.stateCamera.value = State.LOADING
        val intent = Intent(this@DocumentationActivity, CameraActivity::class.java)
        startActivityForResult(intent, Camera.REQUEST_CODE_PERMISSIONS)
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getOutputDirectory()
        return File(
            storageDir,
            "RAW_Hang.jpg"
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun compressImage(uri: Uri) {
        var bitmap: Bitmap? = null
        val contentResolver = contentResolver
        try {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val copyBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)

        val newBitmap = copyBitmap

        var newFile: File? = null
        try {
            newFile = if (isFotoDisplay) {
                File(getOutputDirectory(), "PhotoDisplay.JPEG")
            } else {
                File(getOutputDirectory(), "PhotoLapak.JPEG")
            }

            if (isFotoDisplay) {
                photoDisplayPath = getOutputDirectory().absolutePath + File.separator + "PhotoDisplay.JPEG"
            } else {
                photoLapakPath = getOutputDirectory().absolutePath + File.separator + "PhotoLapak.JPEG"
            }
            newFile.createNewFile()

            if (newFile.exists()) {
                // Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                newBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                val bitmapData = bos.toByteArray()

                // Write the bytes to file
                val fos = FileOutputStream(newFile)
                fos.write(bitmapData)
                fos.flush()
                fos.close()

                newFile.let {
                    lifecycleScope.launch {
                        Compressor.compress(this@DocumentationActivity, it) {
                            default(quality = 80, height = 480)
                            destination(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CompressImage", "Error saat kompresi gambar: ${e.message}", e)
        }

        if (isFotoDisplay) {
            binding.imageViewFotoDisplay.setImageBitmap(newBitmap)
            photoDisplay = newFile
        } else {
            binding.imageViewFotoLapak.setImageBitmap(newBitmap)
            photoLapak = newFile
        }
        viewModel.stateCamera.value = State.COMPLETE
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Camera.REQUEST_CODE_PERMISSIONS) {
            val absolutePath = data?.getStringExtra(KeyIntent.KEY_CAMERA)
            if (!absolutePath.isNullOrEmpty()) {
                val uri = Uri.parse(absolutePath)
                Log.e("uri", uri.toString())
                compressImage(uri)
            } else {
                viewModel.stateCamera.value = State.ERROR
            }
        }
    }

    private fun getLocation() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                locationPermissionCode
            )
        } else {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                viewModel.stateLocation.value = State.LOADING
            } else {
                Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Izin khusus untuk Android 13+
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                // Izin lama untuk Android 11–12
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                locationPermissionCode
            )
        } else {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    private fun getGeocoder(location: Location) {
        // Deteksi apakah location berasal dari mock provider
        isFakeGPS =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && location.isMock) || location.isFromMockProvider

        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = emptyList()

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (addresses != null && addresses.isNotEmpty()) {
            address = addresses[0].getAddressLine(0).toString()
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
            viewModel.stateLocation.value = State.COMPLETE

            if (isFirstOpenCamera) {
                isFirstOpenCamera = false
            }
        } else {
            viewModel.stateLocation.value = State.ERROR
            startLocationUpdates()
        }
    }

    override fun onLocationChanged(p0: Location) {
        Log.e("Location", p0.latitude.toString() + " " + p0.longitude.toString())
        getGeocoder(p0)
    }

    override fun onProviderEnabled(provider: String) {
        Log.e("Location", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.e("Location", "Provider disabled: $provider")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.e("Location", "onStatusChanged: $provider")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED
                && grantResults[4] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Location", "Permission granted")
            } else {
                Toast.makeText(this, "Tolong berikan semua izin untuk aplikasi", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpenCamera) {
            viewModel.stateLocation.value = State.LOADING
        }
        startLocationUpdates()
    }
}