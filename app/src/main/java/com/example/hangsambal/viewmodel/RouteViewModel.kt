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
    val top5Shops = MutableLiveData<List<GetShopData>>()

    fun getTop5RecommendedShops(
        context: Context,
        latitude: String,
        longitude: String,
        page: Int = 1
    ) {
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
                        val top5 = response.body()?.dataShop.orEmpty().take(5)
                        top5Shops.value = top5
                    } else {
                        top5Shops.value = emptyList()
                    }
                }

                override fun onFailure(call: Call<GetShop>, t: Throwable) {
                    top5Shops.value = emptyList()
                }
            })
    }
}