package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetCekPickupFaktur
import com.example.hangsambal.model.response.GetPickup
import com.example.hangsambal.model.response.GetPickupProductData
import com.example.hangsambal.model.response.GetProduct
import com.example.hangsambal.model.response.GetProductData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SpreadingViewModel : BaseViewModel() {
    var stateProduct = MutableLiveData<State>()
    var stateCekPickup = MutableLiveData<State>()
    var statePickupProduct = MutableLiveData<State>()
    var products = MutableLiveData<List<GetProductData>>()
    var productsPickup = MutableLiveData<List<GetPickupProductData>>()
    var totalProduct = MutableLiveData<Int>()
    var errorMessageCek = MutableLiveData<String>()


    fun getCekPickup(context: Context) {
        stateCekPickup.value = State.LOADING
        NetworkClient().getService(context)
            .getCekPickup(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetCekPickupFaktur> {
                override fun onResponse(
                    call: Call<GetCekPickupFaktur>,
                    response: Response<GetCekPickupFaktur>
                ) {
                    if (response.isSuccessful) {
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

                override fun onFailure(call: Call<GetCekPickupFaktur>, t: Throwable) {
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
                        if (response.body()?.dataPickup == null) {
                            statePickupProduct.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        } else {
                            statePickupProduct.value = State.COMPLETE
                            productsPickup.value = response.body()?.dataPickup?.products?.sortedBy { it.idProduct.toString().toInt() }?.toMutableList()
                            if (totalProduct(response.body()?.dataPickup?.products) <= 0) {
                                errorMessageCek.value = "Silahkan melakukan pengambilan produk atau faktur terlebih dahulu"
                            } else {
                                getProduct(context)
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

    fun getProduct(context: Context) {
        stateProduct.value = State.LOADING
        NetworkClient().getService(context)
            .getProduct(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetProduct> {
                override fun onResponse(call: Call<GetProduct>, response: Response<GetProduct>) {
                    if (response.isSuccessful) {
                        if (response.body()?.dataProduct.isNullOrEmpty()) {
                            stateProduct.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        } else {
                            stateProduct.value = State.COMPLETE
                            products.value = response.body()?.dataProduct?.sortedBy { it.idProduct.toString().toInt() }?.toMutableList()
//                            getProductPickup(context)
                        }
                    } else {
                        stateProduct.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetProduct>, t: Throwable) {
                    stateProduct.value = State.ERROR
                    errorMessage.value =  t.message.toString()
                }
            })
    }

    fun totalProduct(products: List<GetPickupProductData>?): Int {
        var total = 0
        products?.forEach {
            if (it.qtyProduct.toString().toInt() >= 0) {
                total += it.qtyProduct.toString().toInt()
            }
        }
        return total
    }
}