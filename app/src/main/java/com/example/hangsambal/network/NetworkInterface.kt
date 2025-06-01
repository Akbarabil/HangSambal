package com.example.hangsambal.network

import com.example.hangsambal.model.request.PostPickup
import com.example.hangsambal.model.response.Base
import com.example.hangsambal.model.response.GetCekPickupFaktur
import com.example.hangsambal.model.response.GetDashboardV2
import com.example.hangsambal.model.response.GetHistory
import com.example.hangsambal.model.response.GetPickup
import com.example.hangsambal.model.response.GetPresence
import com.example.hangsambal.model.response.GetProduct
import com.example.hangsambal.model.response.GetShop
import com.example.hangsambal.model.response.PostPresence
import com.example.hangsambal.model.response.PostShop
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

    @GET("pickup")
    fun getPickup(
        @Header("Authorization") token: String
    ): Call<GetPickup>

    @GET("shop")
    fun getShop(
        @Header("Authorization") token: String,
        @Query("lat_user") latitude: String,
        @Query("lng_user") longitude: String,
        @Query("page") page: String
    ): Call<GetShop>

    @GET("shop/rec")
    fun getShopRecommendation(
        @Header("Authorization") token: String,
        @Query("lat_user") latitude: String,
        @Query("lng_user") longitude: String,
        @Query("page") page: String
    ): Call<GetShop>

    @GET("dashboard/all")
    fun getDashboardV2(
        @Header("Authorization") token: String
    ): Call<GetDashboardV2>

    @POST("shop")
    @Multipart
    fun postShop(
        @Header("Authorization") token: String,
        @Part("id_district") idDistrict: String,
        @Part("name_shop") nameShop: String,
        @Part("owner_shop") ownerShop: String,
        @Part("isinside_market") isInsideMarket: String,
        @Part("type_shop") typeShop: String,
        @Part("detloc_shop") detLocShop: String,
        @Part("telp_shop") telpShop: String,
        @Part("lat_shop") latShop: String,
        @Part("long_shop") longShop: String,
        @Part("kecamatan") kecamatan: String,
        @Part photoSHop: MultipartBody.Part,
    ): Call<PostShop>
}