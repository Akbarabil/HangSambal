package com.example.hangsambal.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hangsambal.R
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.viewmodel.SplashViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
private val viewModel = SplashViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            showToast("Android Anda tidak support!")
            finishAffinity()
            return
        }

        lifecycleScope.launch {
            delay(1500)

            if (checkInternetConnection()) {
                requestPermissionsWithDexter()
            }
        }
    }

    private suspend fun checkInternetConnection(): Boolean {
        while (!isInternetAvailable()) {
            showToast("Menunggu koneksi internet...")
            delay(3000)
        }
        return true
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun requestPermissionsWithDexter() {
        val permissions = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addAll(
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            } else {
                addAll(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )
            }
        }

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        when {
                            it.areAllPermissionsGranted() -> {
                                lifecycleScope.launch { intentToMain() }
                            }

                            it.isAnyPermissionPermanentlyDenied -> {
                                redirectToSettings()
                            }

                            else -> {
                                showToast("Tolong berikan semua izin untuk aplikasi")
                                finish()
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private suspend fun intentToMain() {
        if (Prefs(this).jwt.isNullOrEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            viewModel.checkPresence(this)
            viewModel.alreadyPresence.observe(this) { presenceStatus ->
                when (presenceStatus) {
                    0, 1 -> {
                        Prefs(this).apply {
                            jwt = null
                            idDistrict = null
                        }
                        startActivity(Intent(this, LoginActivity::class.java))
                    }

                    2 -> startActivity(Intent(this, MainActivity::class.java))
                    else -> showToast("Terjadi kesalahan saat memeriksa status kehadiran.")
                }
                finish()
            }
        }
    }

    private fun redirectToSettings() {
        showToast("Tolong berikan semua izin untuk aplikasi")
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        })
        finish()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
