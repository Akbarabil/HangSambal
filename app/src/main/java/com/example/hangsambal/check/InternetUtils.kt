package com.example.hangsambal.check

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

object InternetUtils {
    suspend fun getInternetQuality(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "TIDAK ADA"

        val network = connectivityManager.activeNetwork ?: return "TIDAK ADA"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "TIDAK ADA"

        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return "TIDAK ADA"
        }

        val uploadSpeed = capabilities.linkUpstreamBandwidthKbps ?: 0

        return when {
            uploadSpeed < 10 -> "TIDAK ADA"
            uploadSpeed in 11..30 -> "LEMAH"
            else -> "BAIK"
        }
    }

    fun checkInternetBeforeAction(context: Context, action: () -> Unit) {
        (context as? ComponentActivity)?.lifecycleScope?.launch {
            when (getInternetQuality(context)) {
                "TIDAK ADA" -> AlertDialog.Builder(context)
                    .setTitle("Koneksi Tidak Tersedia")
                    .setMessage("Silahkan periksa koneksi internet Anda dan coba lagi.")
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()

                "LEMAH" -> {
                    Toast.makeText(context, "Internet lemah, memproses", Toast.LENGTH_LONG).show()
                    action()
                }

                "BAIK" -> action()
            }
        }
    }
}