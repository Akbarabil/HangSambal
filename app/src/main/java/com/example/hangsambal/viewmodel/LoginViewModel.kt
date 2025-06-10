package com.example.hangsambal.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.LogIn
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : BaseViewModel() {
    var stateLogin = MutableLiveData<State>()
    var isSuccessLogIn = MutableLiveData<Boolean>()
    var jwt = MutableLiveData<String?>()
    var alreadyPresence = MutableLiveData<Boolean>()

    fun logIn(context: Context, username: String, password: String) {
        stateLogin.value = State.LOADING
        NetworkClient().getService(context)
            .logIn(username, password)
            .enqueue(object : Callback<LogIn> {
                override fun onResponse(call: Call<LogIn>, response: Response<LogIn>) {
                    if (response.body()?.dataLogIn != null) {
                        isSuccessLogIn.value = response.isSuccessful
                        jwt.value = response.body()?.dataLogIn?.jwt
                        checkPresence(context)
                    } else {
                        isSuccessLogIn.value = response.isSuccessful
                        stateLogin.value = State.ERROR
                        val message = response.body()?.statusMessage
                            ?: "Login gagal. Silakan periksa kembali username dan password Anda."
                        errorMessage.value = message
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LogIn>, t: Throwable) {
                    isSuccessLogIn.value = false
                    stateLogin.value = State.ERROR
                    val message = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
                    errorMessage.value = message
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    handleFailure(t)
                }
            })
    }

    fun checkPresence(context: Context) {
        NetworkClient().getService(context)
            .getPresence(jwt.value.toString())
            .enqueue(object : Callback<GetPresence> {
                override fun onResponse(call: Call<GetPresence>, response: Response<GetPresence>) {
                    stateLogin.value = State.COMPLETE

                    if (response.body()?.dataPresence != null) {
                        alreadyPresence.value = response.body()?.dataPresence?.isNotEmpty()
                    } else {
                        alreadyPresence.value = false
                    }

                    if (!response.body()?.dataPresence.isNullOrEmpty()) {
                        Prefs(context).jwt = jwt.value.toString()
                        Prefs(context).idDistrict =
                            response.body()?.dataPresence!!.firstOrNull()?.idDistrict
                    }
                }

                override fun onFailure(call: Call<GetPresence>, t: Throwable) {
                    alreadyPresence.value = false
                    stateLogin.value = State.ERROR
                    val message = "Gagal memeriksa data presensi. Silakan coba lagi nanti."
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            })
    }
}
