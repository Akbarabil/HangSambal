package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.request.PostTransaction
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

class DocumentationViewModel : BaseViewModel() {
    var stateCamera = MutableLiveData<State>()
    var stateLocation = MutableLiveData<State>()
    var stateTransaction = MutableLiveData<State>()
    var stateImageTransaction = MutableLiveData<State>()
    var idTrans = MutableLiveData<String>()
    var errorMessageImage = MutableLiveData<String>()

    fun postTransaction(context: Context, postTransaction: PostTransaction, imageDisplay: File, imageLapak: File) {
        stateTransaction.value = State.LOADING
        NetworkClient().getService(context)
            .postTransaction(
                Prefs(context).jwt.toString(),
                postTransaction
            )
            .enqueue(object : Callback<com.example.hangsambal.model.response.PostTransaction> {
                override fun onResponse(call: Call<com.example.hangsambal.model.response.PostTransaction>, response: Response<com.example.hangsambal.model.response.PostTransaction>) {
                    if (response.isSuccessful) {
                        if (response.body()?.dataPostTransaction != null) {
                            stateTransaction.value = State.COMPLETE
                            idTrans.value = response.body()?.dataPostTransaction?.idTrans.toString()
                            postImageTransaction(context, response.body()?.dataPostTransaction?.idTrans.toString(), imageDisplay, imageLapak)
                        } else {
                            stateTransaction.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        }
                    } else {
                        stateTransaction.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<com.example.hangsambal.model.response.PostTransaction>, t: Throwable) {
                    stateTransaction.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

    fun postImageTransaction(context: Context, idTrans: String, imageDisplay: File, imageLapak: File) {
        stateImageTransaction.value = State.LOADING
        NetworkClient().getService(context)
            .postImageSpreading(
                Prefs(context).jwt.toString(),
                idTrans,
                MultipartBody.Part.createFormData("image_display", imageDisplay.getName(), RequestBody.create("image/*".toMediaTypeOrNull(), imageDisplay)),
                MultipartBody.Part.createFormData("image_lapak", imageLapak.getName(), RequestBody.create("image/*".toMediaTypeOrNull(), imageLapak))
            ).enqueue(object : Callback<com.example.hangsambal.model.response.PostTransaction> {
                override fun onResponse(call: Call<com.example.hangsambal.model.response.PostTransaction>, response: Response<com.example.hangsambal.model.response.PostTransaction>) {
                    if (response.isSuccessful) {
                        if (response.body()?.dataPostTransaction != null) {
                            stateImageTransaction.value = State.COMPLETE
                        } else {
                            stateImageTransaction.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Silakan coba lagi."
                        }
                    } else {
                        stateImageTransaction.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<com.example.hangsambal.model.response.PostTransaction>, t: Throwable) {
                    stateImageTransaction.value = State.ERROR
                    handleFailure(t)
                }
            })
    }
}