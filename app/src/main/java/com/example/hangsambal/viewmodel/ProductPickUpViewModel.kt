package com.example.hangsambal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hangsambal.view.activity.ProductPickupActivity

class ProductPickupViewModel : ViewModel() {

    fun postPickup(
        context: Context,
        products: List<ProductPickupActivity.PostProductPickup>,
        latitude: Double,
        longitude: Double
    ) {
        // Simulasi POST ke API
        Log.d("POST_API", "Kirim data: $products\nLokasi: $latitude, $longitude")
        // Lanjutkan dengan Retrofit atau Volley sesuai implementasi kamu
    }
}