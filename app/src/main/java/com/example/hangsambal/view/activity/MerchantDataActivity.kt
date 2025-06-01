package com.example.hangsambal.view.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.hangsambal.R
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityMerchantDataBinding
import com.example.hangsambal.util.Camera
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.MerchantDataViewModel
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
import java.util.Locale

class MerchantDataActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityMerchantDataBinding
    private lateinit var viewModel: MerchantDataViewModel
    private lateinit var locationManager: LocationManager
    private lateinit var dialog: ProgressDialog
    private lateinit var dialogLocation: ProgressDialog
    private var address: String? = null
    private var isInsideMarket: Int? = 1
    private var typeShop: String? = "Pedagang Sayur"
    private var latitude: String? = null
    private var longitude: String? = null
    private var photoShop: File? = null
    private var currentPhotoPath: String = ""
    private var isFirstOpenCamera: Boolean = true
    private var kecamatan: String = ""

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var locationCallback: LocationCallback
    val locationPermissionCode = 2
    val REQUEST_CODE = 200

    private var isFakeGPS: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMerchantDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)
        dialogLocation = ProgressDialog(this)

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
                        this@MerchantDataActivity,
                        "Tidak dapat mendapatkan lokasi, silahkan coba lagi",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
        viewModel = ViewModelProvider(this).get(MerchantDataViewModel::class.java)
        checkSelfPermission()

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            startLocationUpdates()
            binding.imageViewRefresh.visibility = View.GONE
        } else {
            Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
            binding.imageViewRefresh.visibility = View.VISIBLE
        }

        binding.imageViewRefresh.setOnClickListener {
            startLocationUpdates()
        }

        binding.radioGroupLokasi.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonDalamPasar -> {
                    isInsideMarket = 1

                    binding.radioGroupJenisDalamPasar.visibility = View.VISIBLE
                    binding.radioGroupJenisLuarPasar.visibility = View.GONE

                    binding.radioButtonPermanen.isChecked = true
                    typeShop = "Permanen"
                }

                R.id.radioButtonLuarPasar -> {
                    isInsideMarket = 0

                    binding.radioGroupJenisDalamPasar.visibility = View.GONE
                    binding.radioGroupJenisLuarPasar.visibility = View.VISIBLE

                    binding.radioButtonPedagangSayur.isChecked = true
                    typeShop = "Pedagang Sayur"
                }
            }
        }

        binding.radioGroupJenisDalamPasar.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonPermanen -> {
                    typeShop = "Permanen"
                }

                R.id.radioButtonLoss -> {
                    typeShop = "Loss"
                }
            }
        }

        binding.radioGroupJenisLuarPasar.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonPedagangSayur -> {
                    typeShop = "Pedagang Sayur"
                }

                R.id.radioButtonRetail -> {
                    typeShop = "Retail"
                }
            }
        }

        binding.cardViewFotoLapak.setOnClickListener {
            if (address.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "Mohon dapatkan informasi kecamatan terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                openCamera()
            }
        }

        binding.materialButtonSimpan.setOnClickListener {
            InternetUtils.checkInternetBeforeAction(this) {
                if (address.isNullOrEmpty()) {
                    showAlertDialog("Mohon dapatkan informasi kecamatan terlebih dahulu")
                } else {
                    if (photoShop == null) {
                        showAlertDialog("Mohon ambil foto terlebih dahulu")
                    } else {
                        if (binding.editTextDetailLokasi.text.isNullOrEmpty() || binding.editTextDetailLokasi.text.isNullOrBlank()) {
                            binding.editTextDetailLokasi.error = "Mohon isi detail lokasi"
                            Toast.makeText(this, "Mohon isi detail lokasi", Toast.LENGTH_SHORT)
                                .show()
                        } else if (binding.editTextNamaToko.text.isNullOrEmpty() || binding.editTextNamaToko.text.isNullOrBlank()) {
                            binding.editTextNamaToko.error = "Mohon isi nama toko"
                            Toast.makeText(this, "Mohon isi nama toko", Toast.LENGTH_SHORT).show()
                        } else if (binding.editTextNamaPemilik.text.isNullOrEmpty() || binding.editTextNamaPemilik.text.isNullOrBlank()) {
                            binding.editTextNamaPemilik.error = "Mohon isi nama pemilik"
                            Toast.makeText(this, "Mohon isi nama pemilik", Toast.LENGTH_SHORT)
                                .show()
                        } else if (binding.editTextNoHP.text.isNullOrEmpty() || binding.editTextNoHP.text.isNullOrBlank() || binding.editTextNoHP.text.length < 10) {
                            if (binding.editTextNoHP.text.length < 10) {
                                binding.editTextNoHP.error = "Mohon isi nomor HP dengan benar"
                                Toast.makeText(
                                    this,
                                    "Mohon isi nomor HP dengan benar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                binding.editTextNoHP.error = "Mohon isi nomor HP"
                                Toast.makeText(this, "Mohon isi nomor HP", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            binding.materialButtonSimpan.isEnabled = false
                            viewModel.postShop(
                                this,
                                binding.editTextNamaToko.text.toString(),
                                binding.editTextNamaPemilik.text.toString(),
                                isInsideMarket.toString(),
                                typeShop.toString(),
                                binding.editTextDetailLokasi.text.toString(),
                                binding.editTextNoHP.text.toString(),
                                latitude.toString(),
                                longitude.toString(),
                                kecamatan,
                                photoShop!!
                            )
                        }
                    }
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
                    showAlertDialog("Terjadi kesalahan saat mendapatkan lokasi, mohon coba lagi")
                }
            }
        }

        viewModel.stateCamera.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }

                State.LOADING -> {
                    showProgressDialog()
                }

                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.stateShop.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                    binding.materialButtonSimpan.isEnabled = true
                }

                State.LOADING -> {
                    showProgressDialog()
                    binding.materialButtonSimpan.isEnabled = false
                }

                else -> {
                    dialog.dismiss()
                    binding.materialButtonSimpan.isEnabled = true
                }
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString())
            }
        }

        viewModel.idShop.observe(this) {
            if (!it.isNullOrEmpty()) {
                val intent = Intent(this, FeedbackActivity::class.java)
//                intent.putExtra(KeyIntent.KEY_ID_SHOP, it.toString())
                startActivity(intent)
                finish()
            }
        }
    }

    private fun openCamera() {
        viewModel.stateCamera.value = State.LOADING
        val intent = Intent(this@MerchantDataActivity, CameraActivity::class.java)
        startActivityForResult(intent, Camera.REQUEST_CODE_PERMISSIONS)
    }

    private fun showProgressDialog() {
        //show dialog
        dialog.setMessage("Mohon tunggu...")
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showProgressDialogLocation() {
        //show dialog
        dialogLocation.setMessage("Sedang mencari lokasi...")
        dialogLocation.setCancelable(false)
        dialogLocation.show()
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
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
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()

        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = emptyList()

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (addresses != null && addresses.isNotEmpty()) {
            address = addresses[0].getAddressLine(0).toString()
            viewModel.stateLocation.value = State.COMPLETE

            address = addresses[0].getAddressLine(0).toString()
            kecamatan = if (addresses[0].locality.toString().contains("kecamatan", true)) {
                addresses[0].locality.toString().replaceFirst("kecamatan ", "", true)
            } else {
                addresses[0].locality.toString()
            }

            binding.imageViewRefresh.visibility = View.GONE
            binding.textViewLokasi.text =
                addresses[0].locality.toString() + ", " + addresses[0].subAdminArea.toString()
            viewModel.stateLocation.value = State.COMPLETE

            if (isFirstOpenCamera) {
                isFirstOpenCamera = false
            }
        } else {
            viewModel.stateLocation.value = State.ERROR
            startLocationUpdates()
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
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val copyBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)

        var newFile: File? = null
        try {
            newFile = File(getOutputDirectory(), "PhotoPresence.JPEG")
            newFile.createNewFile()

            if (newFile.exists()) {
                // Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                copyBitmap?.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    bos
                ) // Menyimpan dalam format JPEG
                val bitmapData = bos.toByteArray()

                // Menulis byte array ke file
                val fos = FileOutputStream(newFile)
                fos.write(bitmapData)
                fos.flush()
                fos.close()

                // Kompresi gambar
                newFile.let {
                    lifecycleScope.launch {
                        Compressor.compress(this@MerchantDataActivity, it) {
                            default(quality = 80, height = 480)
                            destination(it)
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }

        // Menampilkan gambar yang sudah diproses
        binding.imageViewFotoLapak.setImageBitmap(copyBitmap)
        photoShop = newFile
        viewModel.stateCamera.value = State.COMPLETE
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = getOutputDirectory()
        return File(
            storageDir,
            "RAW_FINNA.jpg"
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED
                && grantResults[4] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("Location", "Permission granted")
//                getLocation()
            } else {
                Toast.makeText(this, "Tolong berikan semua izin untuk aplikasi", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
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

    override fun onResume() {
        super.onResume()
        if (isFirstOpenCamera) {
            viewModel.stateLocation.value = State.LOADING
        }
        startLocationUpdates()
    }
}