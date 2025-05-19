package com.example.hangsambal.network

import com.example.hangsambal.model.request.PostPickup
import com.example.hangsambal.model.response.Base
import com.example.hangsambal.model.response.GetCekPickupFaktur
import com.example.hangsambal.model.response.GetHistory
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.GetProduct
import com.example.hangsambal.model.response.PostPresence
import com.example.hangsambal.model.response.SignIn
import okhttp3.MultipartBody
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

    @POST("presence")
    @Multipart
    fun postPresence(
        @Header("Authorization") token: String,
        @Part("kecamatan") kecamatan: String,
        @Part("latitude") latitude: String,
        @Part("longitude") longitude: String,
        @Part image: MultipartBody.Part,
        @Part("fake_status") isFakeGPS: Int,
    ): Call<PostPresence>

    @GET("product")
    fun getProduct(
        @Header("Authorization") token: String
    ): Call<GetProduct>

    @GET("cek/pickup")
    fun getCekPickup(
        @Header("Authorization") token: String
    ): Call<GetCekPickupFaktur>

    @POST("pickup")
    fun postPickup(
        @Header("Authorization") token: String,
        @Body postPickup: PostPickup
    ): Call<Base>

    @GET("transaction/history")
    fun getHistory(
        @Header("Authorization") token: String,
        @Query("page") page: String,
        @Query("tanggal") tanggal: String,
    ): Call<GetHistory>
}