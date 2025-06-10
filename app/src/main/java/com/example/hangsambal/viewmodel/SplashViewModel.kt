package com.example.hangsambal.viewmodel

import android.content.Context
import android.widget.Toast
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

    fun checkPresence(context: Context) {
        NetworkClient().getService(context)
            .getPresence(Prefs(context).jwt.toString())
            .enqueue(object : Callback<GetPresence> {
                override fun onResponse(call: Call<GetPresence>, response: Response<GetPresence>) {
                    if (response.isSuccessful) {
                        val dataPresence = response.body()?.dataPresence
                        if (dataPresence == null) {
                            alreadyPresence.value = 0
                            Toast.makeText(context, "Data kehadiran tidak ditemukan.", Toast.LENGTH_SHORT).show()
                        } else {
                            if (dataPresence.isEmpty()) {
                                alreadyPresence.value = 1
                                Toast.makeText(context, "Belum ada presensi hari ini.", Toast.LENGTH_SHORT).show()
                            } else {
                                alreadyPresence.value = 2
                                Toast.makeText(context, "Presensi sudah terdaftar.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Gagal mengambil data: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GetPresence>, t: Throwable) {
                    Toast.makeText(context, "Terjadi kesalahan jaringan: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
