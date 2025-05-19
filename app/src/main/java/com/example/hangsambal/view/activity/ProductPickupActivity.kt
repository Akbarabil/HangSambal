package com.example.hangsambal.view.activity

import android.Manifest
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangsambal.adapter.ProductPickupAdapter
import com.example.hangsambal.check.InternetUtils
import com.example.hangsambal.databinding.ActivityProductPickUpBinding
import com.example.hangsambal.model.request.PostProductPickup
import com.example.hangsambal.util.KeyIntent
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import com.example.hangsambal.viewmodel.ProductPickupViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProductPickupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductPickUpBinding
    private lateinit var viewModel: ProductPickupViewModel
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dialog : ProgressDialog
    private var adapter = ProductPickupAdapter()
    private var date: String = ""
    private var isPresence = false
    private var latitude: String = ""
    private var longitude: String = ""
    private var isFirstOpen = true

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var locationCallback: LocationCallback
    private val locationPermissionCode = 2

    private var isFakeGPS: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductPickUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this)

        isPresence = intent.getBooleanExtra(KeyIntent.KEY_IS_PRESENCE, false)

        viewModel = ViewModelProvider(this).get(ProductPickupViewModel::class.java)

        checkSelfPermission()
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
                    showAlertDialog("Tidak dapat mendapatkan lokasi, silahkan coba lagi", true)
                }
            }
        }

        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }

        binding.recyclerViewProduk.setHasFixedSize(true)
        binding.recyclerViewProduk.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProduk.adapter = adapter

        viewModel.products.observe(this) {
            val list: ArrayList<PostProductPickup> = arrayListOf()
            if (it != null) {
                it.forEach {
                    val data = PostProductPickup(
                        it.idProduct,
                        it.nameProduct,
                        0
                    )
                    list.add(data)
                }
                adapter.products = list
                adapter.notifyDataSetChanged()
            }
        }

        binding.materialButtonSimpan.setOnClickListener {
            InternetUtils.checkInternetBeforeAction(this) {
                if (adapter.sumTotalPickup() > 0) {
                    binding.materialButtonSimpan.isEnabled = false
                    viewModel.postPickup(this, adapter.products, latitude, longitude)
                } else {
                    showAlertDialog("Jumlah produk tidak boleh kosong", false)
                }
            }
        }

        viewModel.stateProduct.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                }
                State.LOADING -> {
                    showProgressDialog("Sedang mengambil data produk...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.stateLocation.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                    viewModel.getCekPickup(this)
                }
                State.LOADING -> {
                    showProgressDialog("Sedang mencari lokasi...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        
        viewModel.stateCekPickup.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    dialog.dismiss()
                    viewModel.getProduct(this)
                }
                State.LOADING -> {
                    showProgressDialog("Sedang memeriksa data pickup...")
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }

        viewModel.statePickup.observe(this) {
            when (it) {
                State.COMPLETE -> {
                    binding.materialButtonSimpan.isEnabled = true
                    dialog.dismiss()
                    startActivity(Intent(this, FeedbackActivity::class.java))
                    finish()
                }
                State.LOADING -> {
                    binding.materialButtonSimpan.isEnabled = false
                    showProgressDialog("Sedang mengirim data pickup...")
                }
                else -> {
                    dialog.dismiss()
                    binding.materialButtonSimpan.isEnabled = true
                }
            }
        }

        viewModel.errorMessageCek.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), true)
            }
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showAlertDialog(it.toString(), false)
            }
        }

    }


    private fun showProgressDialog(message: String) {
        //show dialog
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showAlertDialog(message: String, isFinish: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesan")
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
            if (isFinish) {
                if (isPresence) {
                    onBackPressed()
                } else {
                    finish()
                }
            }
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
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = emptyList()

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (addresses != null && addresses.isNotEmpty()) {
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()

            if (isFirstOpen) {
                isFirstOpen = false
                viewModel.stateLocation.value = State.COMPLETE
            }
        } else {
            viewModel.stateLocation.value = State.ERROR
            startLocationUpdates()
        }

    }


    override fun onBackPressed() {
        if (isPresence) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpen) {
            viewModel.stateLocation.value = State.LOADING
        }
        startLocationUpdates()
    }
}