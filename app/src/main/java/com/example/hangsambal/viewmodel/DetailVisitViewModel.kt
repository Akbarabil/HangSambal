package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.hangsambal.model.response.GetHistoryDetail
import com.example.hangsambal.model.response.GetHistoryDetailData
import com.example.hangsambal.network.NetworkClient
import com.example.hangsambal.util.Prefs
import com.example.hangsambal.util.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailVisitViewModel: BaseViewModel() {
    var detailState = MutableLiveData<State>()
    var detail = MutableLiveData<GetHistoryDetailData?>()

    fun getDetailVisit(context: Context, idTrans: String) {
        NetworkClient().getService(context)
            .getHistoryDetail(
                Prefs(context).jwt.toString(),
                idTrans
            )
            .enqueue(object : Callback<GetHistoryDetail> {
                override fun onResponse(
                    call: Call<GetHistoryDetail>,
                    response: Response<GetHistoryDetail>
                ) {
                    if (response.isSuccessful) {
                        detailState.value = State.COMPLETE
                        detail.value = response.body()?.dataHistoryDetail
                    } else {
                        detailState.value = State.ERROR
                        errorMessage.value = response.body()?.statusMessage
                            ?: "Terjadi kesalahan. Silakan coba lagi."
                    }
                }

                override fun onFailure(call: Call<GetHistoryDetail>, t: Throwable) {
                    detailState.value = State.ERROR
                    handleFailure(t)
                }
            })
    }

}