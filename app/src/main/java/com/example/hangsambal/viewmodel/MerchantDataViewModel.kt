package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.PostShop
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MerchantDataViewModel : BaseViewModel() {
    var stateCamera = MutableLiveData<State>()
    var stateLocation = MutableLiveData<State>()
    var stateShop = MutableLiveData<State>()
    var idShop = MutableLiveData<String>()

    fun postShop(
        context: Context,
        nameShop: String,
        ownerShop: String,
        isInsideMarket: String,
        typeShop: String,
        detLocShop: String,
        telpShop: String,
        latShop: String,
        longShop: String,
        kecamatan: String,
        photoShop: File
    ) {
        stateShop.value = State.LOADING
        NetworkClient().getService(context)
            .postShop(
                Prefs(context).jwt.toString(),
                Prefs(context).idDistrict.toString().toUpperCase(),
                nameShop.toUpperCase(),
                ownerShop.toUpperCase(),
                isInsideMarket,
                typeShop,
                detLocShop.toUpperCase(),
                telpShop,
                latShop,
                longShop,
                kecamatan,
                MultipartBody.Part.createFormData(
                    "photo_shop",
                    photoShop.getName(),
                    RequestBody.create("image/*".toMediaTypeOrNull(), photoShop)
                )
            )
            .enqueue(object : Callback<PostShop> {
                override fun onResponse(call: Call<PostShop>, response: Response<PostShop>) {
                    if (response.isSuccessful) {
                        if (response.body()?.statusCode == 200) {
                            stateShop.value = State.COMPLETE
                            idShop.value = response.body()?.dataPostShop?.idShop.toString()
                        } else {
                            stateShop.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        }
                    } else {
                        stateShop.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<PostShop>, t: Throwable) {
                    stateShop.value = State.ERROR
                    handleFailure(t)
                }
            })
    }
}