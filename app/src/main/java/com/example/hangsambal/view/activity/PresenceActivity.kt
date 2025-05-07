package com.example.hangsambal.view.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityPresenceBinding
import com.example.hangsambal.util.Camera
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.PresenceViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.io.File
import java.util.Locale

class PresenceActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityPresenceBinding
    private lateinit var viewModel: PresenceViewModel
    private lateinit var dialog: ProgressDialog
    private lateinit var dialogLocation: ProgressDialog
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var address: String? = null
    private var kecamatan: String? = null
    private var latitude: String = ""
    private var longitude: String = ""
    private var photoPresence: File? = null
    private var jwt: String = ""
//    private var isFakeGPS: Boolean = false
    private var isFirstOpenCamera: Boolean = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequestBuilder: LocationSettingsRequest.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Reset agar tutorial selalu muncul saat testing
        MaterialShowcaseView.resetSingleUse(this, "SHOWCASE_PRESENCE")

        binding.root.post {
            showStepByStepTutorials()
        }

        dialog = ProgressDialog(this)
        dialogLocation = ProgressDialog(this)

        binding.materialButtonSimpan.setOnClickListener {
            val intent = Intent(this, ProductPickupActivity::class.java)
            startActivity(intent)
            finish()
        }

//        jwt = if (intent.getStringExtra(KeyIntent.KEY_JWT).toString().isNullOrEmpty()) {
//            Prefs(this).jwt.toString()
//        } else {
//            intent.getStringExtra(KeyIntent.KEY_JWT).toString()
//        }

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
                val loc = p0.lastLocation
                if (loc != null) {
                    getGeocoder(p0.lastLocation!!)
                } else {
                    Toast.makeText(
                        this@PresenceActivity,
                        "Tidak dapat mendapatkan lokasi, silahkan coba lagi",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }

        viewModel = ViewModelProvider(this).get(PresenceViewModel::class.java)
        checkSelfPermission()

        //saat menekan kotak gambar kamera
        binding.cardView.setOnClickListener {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (address.isNullOrEmpty()) {
                    startLocationUpdates()
                } else {
                    openCamera()
                }
            } else {
                Toast.makeText(this, "Mohon aktifkan GPS anda", Toast.LENGTH_SHORT).show()
            }
        }

        //saat menekan tombol simpan
        binding.materialButtonSimpan.setOnClickListener {
            if (photoPresence == null) {
//                showAlertDialog("Mohon ambil foto terlebih dahulu")
                val intent = Intent(this, ProductPickupActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                InternetUtils.checkInternetBeforeAction(this) {
                    binding.materialButtonSimpan.isEnabled = false
                    viewModel.postPresence(
                        this,
                        jwt,
                        photoPresence!!,
                        kecamatan.toString(),
                        latitude,
                        longitude
                    )
                }
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString())
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

        viewModel.statePresence.observe(this) { state ->
            when (state) {
                State.COMPLETE -> {
                    // Menangani state selesai
                    dialog.dismiss()
                    binding.materialButtonSimpan.isEnabled = true
                    val intent = Intent(baseContext, ProductPickupActivity::class.java)
//                    intent.putExtra(KeyIntent.KEY_IS_PRESENCE, true)
                    startActivity(intent)
                    finish()
                }

                State.LOADING -> {
                    // Menampilkan progress dan tetap menonaktifkan tombol
                    showProgressDialog()
                    binding.materialButtonSimpan.isEnabled = false
                }

                else -> {
                    // Menangani state lainnya
                    dialog.dismiss()
                    binding.materialButtonSimpan.isEnabled = true
                }
            }
        }

    }

    private fun showStepByStepTutorials() {
        val config = ShowcaseConfig()
        config.delay = 500

        val sequence = MaterialShowcaseSequence(this, "SHOWCASE_PRESENCE")
        sequence.setConfig(config)

        // Debugging untuk memastikan View tidak null
        Log.d("Showcase", "textViewLokasi: ${binding.textViewLokasi}")
        Log.d("Showcase", "imageView: ${binding.imageView}")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(binding.textViewLokasi)
                .setTitleText("Lokasi")
                .setDismissText("Oke")
                .setContentText("Pastikan lokasi presensi sesuai agar tidak mengulangi")
                .withRectangleShape(false)
                .setShapePadding(8)
                .build()
        )

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(binding.imageView)
                .setTitleText("Dokumentasi")
                .setDismissText("Oke")
                .setContentText("Pastikan menggunakan kamera aplikasi.")
                .withRectangleShape(false)
                .setShapePadding(8)
                .build()
        )

        sequence.start()
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
    //callback dari checkSelfPermission
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
    private fun openCamera() {
        viewModel.stateCamera.value = State.LOADING
//        val intent = Intent(this@PresenceActivity, CameraActivity::class.java)
        startActivityForResult(intent, Camera.REQUEST_CODE_PERMISSIONS)
    }

    private fun getGeocoder(location: Location) {
            val geocoder = Geocoder(this, Locale.getDefault())
            var addresses: List<Address>? = emptyList()
            try {
                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Log.e("address", addresses?.firstOrNull()?.getAddressLine(0).toString())

            if (addresses != null && addresses.isNotEmpty()) {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
                address = addresses[0].getAddressLine(0).toString()
                kecamatan = if (addresses[0].locality.toString().contains("kecamatan", true)) {
                    addresses[0].locality.toString().replaceFirst("kecamatan ", "", true)
                } else {
                    addresses[0].locality.toString()
                }
                binding.textViewLokasi.text = kecamatan.toString()
                viewModel.stateLocation.value = State.COMPLETE
                if (isFirstOpenCamera) {
                    isFirstOpenCamera = false
                }
            } else {
                viewModel.stateLocation.value = State.ERROR
                startLocationUpdates()
            }

    }

//    private fun getGeocoder(location: Location) {
//        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && location.isMock) && !location.isFromMockProvider) {
//            val geocoder = Geocoder(this, Locale.getDefault())
//            var addresses: List<Address>? = emptyList()
//            try {
//                addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            Log.e("address", addresses?.firstOrNull()?.getAddressLine(0).toString())
//
//            if (addresses != null && addresses.isNotEmpty()) {
//                latitude = location.latitude.toString()
//                longitude = location.longitude.toString()
//                address = addresses[0].getAddressLine(0).toString()
//                kecamatan = if (addresses[0].locality.toString().contains("kecamatan", true)) {
//                    addresses[0].locality.toString().replaceFirst("kecamatan ", "", true)
//                } else {
//                    addresses[0].locality.toString()
//                }
//                binding.textViewLokasi.text = kecamatan.toString()
//                viewModel.stateLocation.value = State.COMPLETE
//                if (isFirstOpenCamera) {
//                    isFirstOpenCamera = false
//                }
//            } else {
//                viewModel.stateLocation.value = State.ERROR
//                startLocationUpdates()
//            }
//        } else {
//            if (!isFakeGPS) {
//                isFakeGPS = true
//                viewModel.stateLocation.value = State.COMPLETE
//                showAlertDialogFakeGPS()
//            }
//        }
//    }

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
    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
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

    override fun onLocationChanged(p0: Location) {
        Log.e("Location", p0.latitude.toString() + " " + p0.longitude.toString())
        getGeocoder(p0)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}