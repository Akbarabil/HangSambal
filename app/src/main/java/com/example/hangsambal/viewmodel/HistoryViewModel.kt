package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetHistory
import com.example.hangsambal.model.response.GetHistoryData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryViewModel : BaseViewModel() {
    var historyList = MutableLiveData<List<GetHistoryData>?>()
    var totalPage = MutableLiveData<Int?>()

    fun getHistory(context: Context, page: Int, tanggal: String) {
        NetworkClient().getService(context)
            .getHistory(
                Prefs(context).jwt.toString(),
                page.toString(),
                tanggal
            )
            .enqueue(object : Callback<GetHistory> {
                override fun onResponse(call: Call<GetHistory>, response: Response<GetHistory>) {
                    if (response.isSuccessful) {
                        historyList.value = response.body()?.dataHistory
                        totalPage.value = response.body()?.statusPagination?.firstOrNull()?.totalPage
                    } else {
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetHistory>, t: Throwable) {
                    handleFailure(t)
                }
            })
    }

}