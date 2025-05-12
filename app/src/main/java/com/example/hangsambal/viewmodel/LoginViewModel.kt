package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.SignIn
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : BaseViewModel() {
    var stateLogin = MutableLiveData<State>()
    var isSuccessSignIn = MutableLiveData<Boolean>()
    var jwt = MutableLiveData<String?>()
    var alreadyPresence = MutableLiveData<Boolean>()

    fun signIn(context: Context, username: String, password: String) {
        stateLogin.value = State.LOADING
        NetworkClient().getService(context)
            .signIn(
                username,
                password
            )
            .enqueue(object : Callback<SignIn> {
                override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                    if (response.body()?.dataSignIn != null) {
                        isSuccessSignIn.value = response.isSuccessful
                        jwt.value = response.body()?.dataSignIn?.jwt
                        checkPresence(context)
                    } else {
                        isSuccessSignIn.value = response.isSuccessful
                        stateLogin.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<SignIn>, t: Throwable) {
                    isSuccessSignIn.value = false
                    stateLogin.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

    fun checkPresence(context: Context) {
        NetworkClient().getService(context)
            .getPresence(
                jwt.value.toString()
            )
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
                        Prefs(context).idDistrict = response.body()?.dataPresence!!.firstOrNull()?.idDistrict
                    }
                }

                override fun onFailure(call: Call<GetPresence>, t: Throwable) {
                    alreadyPresence.value = false
                    stateLogin.value = State.ERROR
                }
            })
    }
}