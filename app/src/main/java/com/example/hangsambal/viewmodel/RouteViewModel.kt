package com.example.hangsambal.viewmodel

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetShop
import com.example.hangsambal.model.response.GetShopData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.mapbox.api.optimization.v1.models.OptimizationWaypoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteViewModel : BaseViewModel() {

    val top5Shops = MutableLiveData<List<GetShopData>>()

    fun getTop5RecommendedShops(context: Context, latitude: String, longitude: String, page: Int = 1) {
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

                        if (top5.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Tidak ditemukan toko di sekitar lokasi Anda.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        top5Shops.value = emptyList()
                        Toast.makeText(
                            context,
                            "Gagal memuat rekomendasi toko. Silakan coba lagi.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GetShop>, t: Throwable) {
                    top5Shops.value = emptyList()
                    Toast.makeText(
                        context,
                        "Koneksi gagal. Periksa jaringan Anda.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun getOrderedShopsFromWaypoints(
        waypoints: List<OptimizationWaypoint>,
        shops: List<GetShopData>
    ): List<GetShopData> {
        val matchedShops = mutableSetOf<String>()

        fun findNearestUnmatchedShop(lat: Double?, lon: Double?): GetShopData? {
            if (lat == null || lon == null) return null

            return shops
                .filterNot { it.idShop in matchedShops }
                .minByOrNull { shop ->
                    val shopLat = shop.latShop?.toDoubleOrNull()
                    val shopLon = shop.longShop?.toDoubleOrNull()
                    if (shopLat != null && shopLon != null) {
                        distanceBetween(lat, lon, shopLat, shopLon)
                    } else {
                        Double.MAX_VALUE
                    }
                }?.also {
                    matchedShops.add(it.idShop ?: "")
                }
        }

        return waypoints
            .drop(1)
            .sortedBy { it.waypointIndex() }
            .mapNotNull { waypoint ->
                val lat = waypoint.location()?.latitude()
                val lon = waypoint.location()?.longitude()
                findNearestUnmatchedShop(lat, lon)
            }
    }

    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0].toDouble()
    }
}

