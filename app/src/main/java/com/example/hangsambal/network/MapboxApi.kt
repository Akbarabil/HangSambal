package com.example.hangsambal.network

import com.example.hangsambal.model.request.OptimizationRequest
import com.example.hangsambal.model.response.OptimizationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET


interface MapboxApi {

    @GET("optimized")
    fun getOptimizedRoute(@Body request: OptimizationRequest): Call<OptimizationResponse>
}