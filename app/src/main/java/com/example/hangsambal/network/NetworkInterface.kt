package com.example.hangsambal.network

import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.SignIn
import retrofit2.Call
import retrofit2.http.*

interface NetworkInterface {
    @POST("auth/login")
    @FormUrlEncoded
    fun signIn(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<SignIn>

    @GET("presence/detail")
    fun getPresence(
        @Header("Authorization") token: String,
    ): Call<GetPresence>
}