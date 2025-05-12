package com.example.hangsambal.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.hangsambal.R
import com.example.hangsambal.databinding.ActivitySplashBinding
import com.example.hangsambal.util.Prefs
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Atur fullscreen sebelum menampilkan layout
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)
        Log.d("SplashActivity", "Layout splash telah ditampilkan")

        // ✅ Cek versi Android minimum
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.e("SplashActivity", "Android versi tidak didukung")
            showToast("Android Anda tidak support!")
            finishAffinity()
            return
        }

        // ✅ Delay agar splash screen terlihat dulu
        lifecycleScope.launch {
            delay(1500) // tampilkan splash selama 1.5 detik

            Log.d("SplashActivity", "Memeriksa koneksi internet...")
            if (checkInternetConnection()) {
                Log.d("SplashActivity", "Internet tersedia, lanjut ke permission")
                requestPermissionsWithDexter()
            }
        }
    }

    private suspend fun checkInternetConnection(): Boolean {
        while (!isInternetAvailable()) {
            Log.w("SplashActivity", "Tidak ada koneksi internet. Menunggu...")
            showToast("Menunggu koneksi internet...")
            delay(3000)
        }
        Log.d("SplashActivity", "Koneksi internet tersedia")
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
        Log.d("SplashActivity", "Meminta permission dari pengguna")

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
                        Log.d("SplashActivity", "=== HASIL PERMISSION CHECK ===")
                        Log.d("SplashActivity", "Semua diizinkan? ${it.areAllPermissionsGranted()}")
                        Log.d("SplashActivity", "Jumlah permission diberikan: ${it.grantedPermissionResponses.size}")
                        Log.d("SplashActivity", "Jumlah permission ditolak: ${it.deniedPermissionResponses.size}")

                        it.grantedPermissionResponses.forEach { granted ->
                            Log.d("SplashActivity", "✅ Diizinkan: ${granted.permissionName}")
                        }

                        it.deniedPermissionResponses.forEach { denied ->
                            val permanently = if (denied.isPermanentlyDenied) " (PERMANEN)" else ""
                            Log.w("SplashActivity", "❌ Ditolak: ${denied.permissionName}$permanently")
                        }

                        when {
                            it.areAllPermissionsGranted() -> {
                                Log.d("SplashActivity", "Semua permission diberikan")
                                lifecycleScope.launch { intentToMain() }
                            }
                            it.isAnyPermissionPermanentlyDenied -> {
                                Log.w("SplashActivity", "Ada permission yang ditolak permanen")
                                redirectToSettings()
                            }
                            else -> {
                                Log.e("SplashActivity", "Permission tidak lengkap")
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
                    Log.d("SplashActivity", "Menampilkan rationale permission")
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private suspend fun intentToMain() {
        Log.d("SplashActivity", "Menentukan halaman selanjutnya...")
        if (Prefs(this).jwt.isNullOrEmpty()) {
            Log.d("SplashActivity", "JWT kosong, mengarahkan ke LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            Log.d("SplashActivity", "JWT ditemukan, mengarahkan ke MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    private fun redirectToSettings() {
        Log.w("SplashActivity", "Mengarahkan pengguna ke pengaturan aplikasi")
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