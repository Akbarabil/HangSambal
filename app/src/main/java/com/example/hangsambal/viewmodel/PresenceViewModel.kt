package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.PostPresence
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class PresenceViewModel : BaseViewModel() {
    var stateLocation = MutableLiveData<State>()
    var statePresence = MutableLiveData<State>()
    var stateCamera = MutableLiveData<State>()


    fun postPresence(context: Context, jwt: String, image: File, kecamatan: String, latitude: String, longitude: String, isMock: Int
    ) {
        statePresence.value = State.LOADING
        NetworkClient().getService(context)
            .postPresence(
                jwt,
                kecamatan,
                latitude,
                longitude,
                MultipartBody.Part.createFormData("image", image.getName(),
                    RequestBody.create("image/*".toMediaTypeOrNull(), image)
                ),
                isMock
            )
            .enqueue(object : Callback<PostPresence> {
                override fun onResponse(call: Call<PostPresence>, response: Response<PostPresence>) {
                    if (response.isSuccessful) {
                        if (response.body()?.dataPostPresence != null) {
                            checkPresence(context, jwt)
                        } else {
                            statePresence.value = State.ERROR
                            errorMessage.value = response.body()?.statusMessage
                                ?: "Terjadi kesalahan. Periksa Internet Anda, Silakan coba lagi."
                        }
                    } else {
                        statePresence.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                    }
                }

                override fun onFailure(call: Call<PostPresence>, t: Throwable) {
                    statePresence.value = State.ERROR
                    handleFailure(t)
                }
            })
    }


    fun checkPresence(context: Context, jwt: String) {
        NetworkClient().getService(context)
            .getPresence(
                jwt
            )
            .enqueue(object : Callback<GetPresence> {
                override fun onResponse(call: Call<GetPresence>, response: Response<GetPresence>) {
                    if (response.isSuccessful) {
                        val data = response.body()?.dataPresence
                        if (!data.isNullOrEmpty()) {
                            statePresence.value = State.COMPLETE
                            Prefs(context).jwt = jwt
                            Prefs(context).idDistrict = data.firstOrNull()?.idDistrict
                        } else {
                            statePresence.value = State.ERROR
                            errorMessage.value = "Data Presensi Kosong"
                        }
                    } else {
                        statePresence.value = State.ERROR
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            JSONObject(errorBody ?: "").optString("statusMessage")
                        } catch (e: Exception) {
                            null
                        }
                        errorMessage.value = errorMsg.takeIf { it?.isNotBlank() == true }
                            ?: "Gagal mengambil data presensi. Silakan coba beberapa saat lagi."
                    }
                }

                override fun onFailure(call: Call<GetPresence>, t: Throwable) {
                    statePresence.value = State.ERROR
                    handleFailure(t)
                }
            })
    }
}