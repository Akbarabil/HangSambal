package com.example.hangsambal.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.request.PostPickup
import com.example.hangsambal.model.request.PostProductPickup
import com.example.hangsambal.model.response.Base
import com.example.hangsambal.model.response.GetCekPickupFaktur
import com.example.hangsambal.model.response.GetProduct
import com.example.hangsambal.model.response.GetProductData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductPickupViewModel: BaseViewModel() {
    var stateProduct = MutableLiveData<State>()
    var stateLocation = MutableLiveData<State>()
    var statePickup = MutableLiveData<State>()
    var stateCekPickup = MutableLiveData<State>()
    var products = MutableLiveData<List<GetProductData>>()
    var errorMessageCek = MutableLiveData<String>()

    fun getProduct(context: Context) {
        stateProduct.value = State.LOADING
        val token = Prefs(context).jwt.toString()
        Log.d("VIEWMODEL", "JWT token untuk getProduct: $token") // <-- Tambahkan ini
        NetworkClient().getService(context)
            .getProduct(
                Prefs(context).jwt.toString()
            )
            .enqueue(object : Callback<GetProduct> {
                override fun onResponse(call: Call<GetProduct>, response: Response<GetProduct>) {
                    if (response.isSuccessful) {
                        if (response.body()?.dataProduct.isNullOrEmpty()) {
                            stateProduct.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage?: "Terjadi kesalahan. Silakan coba lagi."
                        } else {
                            stateProduct.value = State.COMPLETE
                            products.value = response.body()?.dataProduct?.sortedBy { it.idProduct.toString().toInt() }?.toMutableList()
                        }
                    } else {
                        stateProduct.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetProduct>, t: Throwable) {
                    stateProduct.value = State.ERROR
                    errorMessage.value =  t.message.toString()
                }
            })
    }


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
                        if (response.body()?.statusSuccess == 1) {
                            stateCekPickup.value = State.COMPLETE
                        } else {
                            stateCekPickup.value = State.ERROR
                            errorMessageCek.value = response.body()?.statusMessage ?: "Anda telah melakukan pickup, silahkan melakukan transaksi atau faktur"
                        }
                    } else {
                        stateCekPickup.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetCekPickupFaktur>, t: Throwable) {
                    stateCekPickup.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

    fun postPickup(context: Context, products: List<PostProductPickup>, latitude: String, longitude: String) {
        statePickup.value = State.LOADING
        val data = PostPickup(
            products,
            latitude,
            longitude
        )
        NetworkClient().getService(context)
            .postPickup(
                Prefs(context).jwt.toString(),
                data
            )
            .enqueue(object : Callback<Base> {
                override fun onResponse(call: Call<Base>, response: Response<Base>) {
                    if (response.isSuccessful) {
                        if (response.body()?.statusCode == 200) {
                            statePickup.value = State.COMPLETE
                        } else {
                            statePickup.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage?: "Terjadi kesalahan. Silakan coba lagi."
                        }
                    } else {
                        statePickup.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<Base>, t: Throwable) {
                    statePickup.value = State.ERROR
                    errorMessage.value =  t.message.toString()
                }
            })
    }
}