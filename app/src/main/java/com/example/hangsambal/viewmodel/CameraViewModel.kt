package com.example.hangsambal.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hangsambal.util.State

class CameraViewModel: ViewModel() {
    var stateCamera = MutableLiveData<State>()
}
