package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.util.State
import java.io.File

class PresenceViewModel : ViewModel() {
    var stateLocation = MutableLiveData<State>()
    var statePresence = MutableLiveData<State>()
    var errorMessage = MutableLiveData<String>()
    var stateCamera = MutableLiveData<State>()


    fun postPresence(context: Context, jwt: String, photo: File, kecamatan: String, latitude: String, longitude: String) {

    }
    fun checkPresence(context: Context, jwt: String) {

    }
}