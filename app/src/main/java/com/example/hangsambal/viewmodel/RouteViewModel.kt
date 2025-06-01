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

class RouteViewModel : ViewModel() {
    val shopList = MutableLiveData<List<GetShopData>?>()

    fun fetchNearbyShops(context: Context, lat: String, lng: String) {
        NetworkClient().getService(context)
            .getShopRecommendation(Prefs(context).jwt.toString(), lat, lng, "1")
            .enqueue(object : Callback<GetShop> {
                override fun onResponse(call: Call<GetShop>, response: Response<GetShop>) {
                    if (response.isSuccessful) {
                        val top5 = response.body()?.dataShop?.sortedBy {
                            it.distanceShop?.toFloatOrNull() ?: Float.MAX_VALUE
                        }?.take(5)
                        shopList.postValue(top5)
                    }
                }

                override fun onFailure(call: Call<GetShop>, t: Throwable) {
                    Log.e("RouteVM", "Failed: ${t.message}")
                }
            })
    }
}