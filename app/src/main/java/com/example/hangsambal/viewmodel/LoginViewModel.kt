package com.example.hangsambal.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.util.State

class LoginViewModel : ViewModel() {
    var stateLogin = MutableLiveData<State>()

    fun signIn(context: Context, username: String, password: String) {
        stateLogin.value = State.LOADING

    }

    fun checkPresence(context: Context) {
        stateLogin.value = State.LOADING

    }
}