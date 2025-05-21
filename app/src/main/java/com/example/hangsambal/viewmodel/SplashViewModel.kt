package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashViewModel : ViewModel() {
    var alreadyPresence = MutableLiveData<Int>()

    fun checkPresence(context: Context, ) {
        NetworkClient().getService(context)
            .getPresence(Prefs(context).jwt.toString())
            .enqueue(object : Callback<GetPresence> {
                override fun onResponse(call: Call<GetPresence>, response: Response<GetPresence>) {
                    if (response.body()?.dataPresence == null) {
                        alreadyPresence.value = 0
                    } else {
                        if (response.body()?.dataPresence.isNullOrEmpty()) {
                            alreadyPresence.value = 1
                        } else {
                            alreadyPresence.value = 2
                        }
                    }
                }
                override fun onFailure(call: Call<GetPresence>, t: Throwable) {

                }

            })
    }
}