package com.example.hangsambal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.model.response.GetShop
import com.example.hangsambal.model.response.GetShopData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteViewModel : BaseViewModel() {

    fun getTop5RecommendedShops(context: Context, latitude: String, longitude: String, page: Int = 1, onResult: (List<GetShopData>) -> Unit) {
        NetworkClient().getService(context)
            .getShopRecommendation(
                Prefs(context).jwt.toString(),
                latitude,
                longitude,
                page.toString()
            )
            .enqueue(object : Callback<GetShop> {
                override fun onResponse(call: Call<GetShop>, response: Response<GetShop>) {
                    if (response.isSuccessful) {
                        val shops = response.body()?.dataShop.orEmpty()
                        val top5 = shops.take(5)
                        onResult(top5)
                    } else {
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                        onResult(emptyList()) // Tetap panggil callback agar tidak menggantung
                    }
                }

                override fun onFailure(call: Call<GetShop>, t: Throwable) {
                    handleFailure(t)
                    onResult(emptyList())
                }
            })
    }
}