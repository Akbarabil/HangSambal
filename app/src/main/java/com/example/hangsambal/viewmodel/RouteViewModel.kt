package com.example.hangsambal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangsambal.model.request.OptimizationRequest
import com.example.hangsambal.model.response.OptimizationResponse
import com.example.hangsambal.network.Retrofit
import kotlinx.coroutines.launch
import okhttp3.Route
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteViewModel : ViewModel() {

    private val _optimizedRoute = MutableLiveData<List<Route>>()
    val optimizedRoute: LiveData<List<Route>> = _optimizedRoute

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

//    fun getOptimizedRoute(request: OptimizationRequest) {
//        viewModelScope.launch {
//            try {
//                val response = Retrofit.api.getOptimizedRoute(request)
//                _optimizedRoute.value = response.routes
//                _errorMessage.value = null  // clear previous error
//            } catch (e: Exception) {
//                _errorMessage.value = "Gagal mengambil rute: ${e.message}"
//                e.printStackTrace()
//            }
//        }
//    }
}