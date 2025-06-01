package com.example.hangsambal.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostShop(
    @SerializedName("data")
    val dataPostShop: PostShopData?
) : Parcelable, Base()

@Parcelize
data class PostShopData(
    @SerializedName("ID_SHOP")
    val idShop: String?
) : Parcelable