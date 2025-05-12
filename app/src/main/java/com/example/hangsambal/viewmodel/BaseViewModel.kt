package com.example.hangsambal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.util.State
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseViewModel : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val state = MutableLiveData<State>()

    fun handleFailure(t: Throwable) {
        val userFriendlyMessage = when (t) {
            is UnknownHostException -> "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
            is SocketTimeoutException -> "Koneksi timeout. Server mungkin sedang sibuk. Coba lagi nanti."
            is IOException -> "Terjadi masalah jaringan. Pastikan Anda terhubung ke internet."
            else -> {
                if (!t.message.isNullOrEmpty()) {
                    t.message.toString()
                } else {
                    "Terjadi kesalahan: ${t.localizedMessage}"
                }
            }
        }

        errorMessage.value = userFriendlyMessage
    }

}