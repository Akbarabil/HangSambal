package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetCekPickupProduk
import com.example.hangsambal.model.response.GetDashboardV2
import com.example.hangsambal.model.response.GetDashboardV2Data
import com.example.hangsambal.model.response.GetPickup
import com.example.hangsambal.model.response.GetPickupProductData
import com.example.hangsambal.model.response.GetShop
import com.example.hangsambal.model.response.GetShopData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : BaseViewModel() {
    var dashboardV2 = MutableLiveData<GetDashboardV2Data>()
    var shopList = MutableLiveData<List<GetShopData>?>()
    var productsPickup = MutableLiveData<List<GetPickupProductData>>()
    var totalPage = MutableLiveData<Int?>()
    var statePickupProduct = MutableLiveData<State>()
    var stateCekPickup = MutableLiveData<State>()
    var messagePickup = MutableLiveData<String?>()


    fun getDashboardV2(context: Context) {
        NetworkClient().getService(context)
            .getDashboardV2(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetDashboardV2> {
                override fun onResponse(
                    call: Call<GetDashboardV2>,
                    response: Response<GetDashboardV2>
                ) {
                    if (response.isSuccessful) {
                        dashboardV2.value = response.body()?.dataDashboardV2?.firstOrNull()
                    }
                }

                override fun onFailure(call: Call<GetDashboardV2>, t: Throwable) {
                    handleFailure(t)
                }
            })
    }

    fun getCekPickup(context: Context) {
        stateCekPickup.value = State.LOADING
        NetworkClient().getService(context)
            .getCekPickup(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetCekPickupProduk> {
                override fun onResponse(
                    call: Call<GetCekPickupProduk>,
                    response: Response<GetCekPickupProduk>
                ) {
                    if (response.isSuccessful) {
                        messagePickup.value = response.body()?.statusMessage
                        if (response.body()?.statusSuccess == 0) {
                            stateCekPickup.value = State.COMPLETE
                            getProductPickup(context)
                        } else {
                            stateCekPickup.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        }
                    } else {
                        stateCekPickup.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetCekPickupProduk>, t: Throwable) {
                    stateCekPickup.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

    fun getProductPickup(context: Context) {
        statePickupProduct.value = State.LOADING
        NetworkClient().getService(context)
            .getPickup(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetPickup> {
                override fun onResponse(call: Call<GetPickup>, response: Response<GetPickup>) {
                    if (response.isSuccessful) {
                        messagePickup.value = response.body()?.statusMessage
                        if (response.body()?.dataPickup == null) {
                            statePickupProduct.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        } else {
                            if (response.body()?.dataPickup?.products.isNullOrEmpty()) {
                                statePickupProduct.value = State.FINISH_PICKUP
                            } else {
                                statePickupProduct.value = State.COMPLETE
                                productsPickup.value = response.body()?.dataPickup?.products?.sortedBy { it.idProduct.toString().toInt() }?.toMutableList()
                            }
                        }
                    } else {
                        statePickupProduct.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetPickup>, t: Throwable) {
                    statePickupProduct.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

    fun getShopRecommendation(context: Context, latitude: String, longitude: String, page: Int) {
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
                        shopList.value = response.body()?.dataShop
                        totalPage.value = response.body()?.statusPagination?.firstOrNull()?.totalPage
                    } else {
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetShop>, t: Throwable) {
                    handleFailure(t)
                }
            })
    }

}